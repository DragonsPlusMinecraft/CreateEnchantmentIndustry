/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.migration;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchanterBehaviour;

/** Migrates final 1.4.1 block-entity fields and sanitizes invalid early-2.5 enchanter samples. */
public final class LegacyBlockEntityData {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyBlockEntityData.class);
    private static final AtomicBoolean WARNED_PRINTER = new AtomicBoolean();
    private static final AtomicBoolean WARNED_BLAZE_ENCHANTER = new AtomicBoolean();
    private static final AtomicBoolean WARNED_INVALID_ENCHANTER_TEMPLATE = new AtomicBoolean();

    private static final String LEGACY_PRINTER_TARGET = "copyTarget";
    private static final String LEGACY_PROCESSING_TIME = "ProcessingTicks";
    private static final String LEGACY_TARGET_ITEM = "TargetItem";
    private static final String HELD_ITEM = "HeldItem";
    private static final String TRANSPORTED_ITEM = "Item";
    private static final String PROCESSING_TIME = "ProcessingTime";
    private static final String ACTIVE_ENCHANTING = "ActiveEnchanting";

    private LegacyBlockEntityData() {}

    public static CompoundTag migratePrinter(CompoundTag source) {
        if (source.contains(PrinterBehaviour.TEMPLATE, Tag.TAG_COMPOUND)
                || !source.contains(LEGACY_PRINTER_TARGET, Tag.TAG_COMPOUND)) {
            return source;
        }
        CompoundTag migrated = source.copy();
        migrated.put(PrinterBehaviour.TEMPLATE, source.getCompound(LEGACY_PRINTER_TARGET).copy());
        warnOnce(WARNED_PRINTER, "printer");
        return migrated;
    }

    public static CompoundTag migrateBlazeEnchanter(CompoundTag source) {
        boolean legacyProcessingTime = source.contains(LEGACY_PROCESSING_TIME, Tag.TAG_INT);
        boolean legacyHeldItem = source.contains(HELD_ITEM, Tag.TAG_COMPOUND)
                && source.getCompound(HELD_ITEM).contains(TRANSPORTED_ITEM, Tag.TAG_COMPOUND);
        boolean legacyTarget = source.contains(LEGACY_TARGET_ITEM, Tag.TAG_COMPOUND);
        boolean invalidTemplate = hasInvalidEnchantingTemplate(source);
        if (!legacyProcessingTime && !legacyHeldItem && !legacyTarget && !invalidTemplate) {
            return source;
        }

        CompoundTag migrated = source.copy();
        if (legacyHeldItem) {
            migrated.put(HELD_ITEM, source.getCompound(HELD_ITEM).getCompound(TRANSPORTED_ITEM).copy());
        }
        migrated.remove(LEGACY_PROCESSING_TIME);
        migrated.remove(LEGACY_TARGET_ITEM);

        boolean legacyData = legacyProcessingTime || legacyHeldItem || legacyTarget;
        if (legacyData || invalidTemplate) {
            migrated.putInt(PROCESSING_TIME, -1);
            migrated.remove(ACTIVE_ENCHANTING);
        }
        if (legacyTarget || invalidTemplate) {
            migrated.remove(EnchanterBehaviour.TEMPLATE);
            migrated.putInt(EnchanterBehaviour.LEVEL, 0);
        }

        if (legacyData) {
            warnOnce(WARNED_BLAZE_ENCHANTER, "blaze_enchanter");
        }
        if (invalidTemplate && WARNED_INVALID_ENCHANTER_TEMPLATE.compareAndSet(false, true)) {
            LOGGER.warn("Discarded an invalid blaze enchanter sample while loading; the interrupted operation was cancelled");
        }
        return migrated;
    }

    private static boolean hasInvalidEnchantingTemplate(CompoundTag source) {
        if (!source.contains(EnchanterBehaviour.TEMPLATE, Tag.TAG_COMPOUND)) {
            return false;
        }
        ItemStack template = ItemStack.of(source.getCompound(EnchanterBehaviour.TEMPLATE));
        return !template.isEmpty() && !template.isEnchantable();
    }

    private static void warnOnce(AtomicBoolean warned, String id) {
        if (warned.compareAndSet(false, true)) {
            LOGGER.warn("Migrated legacy 1.4.1 block-entity data for create_enchantment_industry:{}; save the world to write the 2.5 format", id);
        }
    }
}
