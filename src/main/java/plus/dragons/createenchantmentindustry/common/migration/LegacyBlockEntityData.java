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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchanterBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

/** Converts block-entity fields written by the final 1.4.1 build into the 2.5 format. */
public final class LegacyBlockEntityData {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyBlockEntityData.class);
    private static final AtomicBoolean WARNED_PRINTER = new AtomicBoolean();
    private static final AtomicBoolean WARNED_BLAZE_ENCHANTER = new AtomicBoolean();

    private static final String LEGACY_PRINTER_TARGET = "copyTarget";
    private static final String LEGACY_PROCESSING_TIME = "ProcessingTicks";
    private static final String LEGACY_TARGET_ITEM = "TargetItem";
    private static final String HELD_ITEM = "HeldItem";
    private static final String TRANSPORTED_ITEM = "Item";
    private static final String PROCESSING_TIME = "ProcessingTime";

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
        boolean legacyProcessingTime = !source.contains(PROCESSING_TIME, Tag.TAG_INT)
                && source.contains(LEGACY_PROCESSING_TIME, Tag.TAG_INT);
        boolean legacyHeldItem = source.contains(HELD_ITEM, Tag.TAG_COMPOUND)
                && source.getCompound(HELD_ITEM).contains(TRANSPORTED_ITEM, Tag.TAG_COMPOUND);
        boolean legacyTarget = !source.contains(EnchanterBehaviour.TEMPLATE, Tag.TAG_COMPOUND)
                && source.contains(LEGACY_TARGET_ITEM, Tag.TAG_COMPOUND);
        if (!legacyProcessingTime && !legacyHeldItem && !legacyTarget) {
            return source;
        }

        CompoundTag migrated = source.copy();
        if (legacyProcessingTime) {
            migrated.putInt(PROCESSING_TIME, source.getInt(LEGACY_PROCESSING_TIME));
        }
        if (legacyHeldItem) {
            migrated.put(HELD_ITEM, source.getCompound(HELD_ITEM).getCompound(TRANSPORTED_ITEM).copy());
        }
        if (legacyTarget) {
            LegacyEnchantingTarget target = convertEnchantingGuide(source.getCompound(LEGACY_TARGET_ITEM));
            if (target != null) {
                migrated.put(EnchanterBehaviour.TEMPLATE, target.template().save(new CompoundTag()));
                if (!migrated.contains(EnchanterBehaviour.LEVEL, Tag.TAG_INT)) {
                    migrated.putInt(EnchanterBehaviour.LEVEL, target.enchantingPower());
                }
            }
        }
        warnOnce(WARNED_BLAZE_ENCHANTER, "blaze_enchanter");
        return migrated;
    }

    private static LegacyEnchantingTarget convertEnchantingGuide(CompoundTag guideStack) {
        if (!guideStack.contains("tag", Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag guideData = guideStack.getCompound("tag");
        if (!guideData.contains("target", Tag.TAG_COMPOUND)) {
            return null;
        }
        ItemStack targetBook = ItemStack.of(guideData.getCompound("target"));
        var enchantments = new ArrayList<>(CEIItemData.getEnchantmentsForCrafting(targetBook).entrySet());
        if (enchantments.isEmpty()) {
            return null;
        }
        int index = Mth.clamp(guideData.getInt("index"), 0, enchantments.size() - 1);
        Map.Entry<Enchantment, Integer> selected = enchantments.get(index);
        ItemStack template = new ItemStack(CEIItems.ENCHANTING_TEMPLATE.get());
        CEIItemData.setStoredEnchantments(template, Map.of(selected.getKey(), selected.getValue()));
        int enchantingPower = Math.max(1, selected.getKey().getMinCost(selected.getValue()));
        return new LegacyEnchantingTarget(template, enchantingPower);
    }

    private static void warnOnce(AtomicBoolean warned, String id) {
        if (warned.compareAndSet(false, true)) {
            LOGGER.warn("Migrated legacy 1.4.1 block-entity data for create_enchantment_industry:{}; save the world to write the 2.5 format", id);
        }
    }

    private record LegacyEnchantingTarget(ItemStack template, int enchantingPower) {}
}
