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

package plus.dragons.createenchantmentindustry.common.item;

import com.simibubi.create.content.logistics.box.PackageItem;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central item-data bridge for Minecraft 1.20.1.
 *
 * <p>Vanilla and Create data stay in their native NBT formats. CEI-owned data is isolated below the
 * {@value #ROOT_TAG} compound and guarded by an explicit schema version.</p>
 */
public final class CEIItemData {
    public static final String ROOT_TAG = "create_enchantment_industry";
    public static final String SCHEMA_VERSION_TAG = "schema_version";
    public static final int SCHEMA_VERSION = 1;
    public static final String AFFIX_TEMPLATE_TAG = "affix_template";
    public static final String OVERLIMIT_AFFIXES_TAG = "overlimit_affixes";
    public static final String STORED_ENTITIES_TAG = "stored_entities";

    private static final Logger LOGGER = LoggerFactory.getLogger(CEIItemData.class);
    private static final Set<Integer> WARNED_UNKNOWN_SCHEMAS = ConcurrentHashMap.newKeySet();

    private CEIItemData() {}

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack)));
    }

    public static void setEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (stack.getItem() instanceof EnchantedBookItem)
            setStoredEnchantments(stack, enchantments);
        else
            EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    public static Map<Enchantment, Integer> getStoredEnchantments(ItemStack stack) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(
                EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(stack))));
    }

    public static void setStoredEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        stack.removeTagKey("StoredEnchantments");
        enchantments.forEach((enchantment, level) -> EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, level)));
    }

    /** Returns native stored enchantments for CEI templates, otherwise the stack's normal/book enchantments. */
    public static Map<Enchantment, Integer> getEnchantmentsForCrafting(ItemStack stack) {
        Map<Enchantment, Integer> stored = getStoredEnchantments(stack);
        return stored.isEmpty() ? getEnchantments(stack) : stored;
    }

    public static int getRepairCost(ItemStack stack) {
        return stack.getBaseRepairCost();
    }

    public static void setRepairCost(ItemStack stack, int cost) {
        stack.setRepairCost(Math.max(0, cost));
    }

    public static @Nullable Component getCustomName(ItemStack stack) {
        return stack.hasCustomHoverName() ? stack.getHoverName() : null;
    }

    public static void setCustomName(ItemStack stack, @Nullable Component name) {
        if (name == null) {
            stack.resetHoverName();
        } else {
            stack.setHoverName(name);
        }
    }

    public static String getPackageAddress(ItemStack stack) {
        return PackageItem.getAddress(stack);
    }

    public static void setPackageAddress(ItemStack stack, String address) {
        if (address.isEmpty()) {
            PackageItem.clearAddress(stack);
        } else {
            PackageItem.addAddress(stack, address);
        }
    }

    /** Creates a stack of a different item while preserving all 1.20.1 item NBT. */
    public static ItemStack transmuteCopy(ItemStack source, Item target) {
        ItemStack result = new ItemStack(target, source.getCount());
        if (source.hasTag()) {
            result.setTag(source.getTag().copy());
        }
        return result;
    }

    public static @Nullable CompoundTag getOwnedData(ItemStack stack, String key) {
        CompoundTag root = getRoot(stack, false);
        return root != null && root.contains(key, Tag.TAG_COMPOUND) ? root.getCompound(key).copy() : null;
    }

    public static boolean setOwnedData(ItemStack stack, String key, @Nullable CompoundTag value) {
        CompoundTag root = getRoot(stack, value != null);
        if (root == null) {
            return false;
        }
        if (value == null) {
            root.remove(key);
        } else {
            root.put(key, value.copy());
        }
        return true;
    }

    private static @Nullable CompoundTag getRoot(ItemStack stack, boolean create) {
        CompoundTag itemTag = create ? stack.getOrCreateTag() : stack.getTag();
        if (itemTag == null) {
            return null;
        }
        CompoundTag root;
        if (itemTag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            root = itemTag.getCompound(ROOT_TAG);
            int schema = root.contains(SCHEMA_VERSION_TAG) ? root.getInt(SCHEMA_VERSION_TAG) : 0;
            if (schema > SCHEMA_VERSION) {
                if (WARNED_UNKNOWN_SCHEMAS.add(schema)) {
                    LOGGER.warn("Ignoring CEI item data with unsupported schema version {} (supported: {})", schema, SCHEMA_VERSION);
                }
                return null;
            }
            if (schema < SCHEMA_VERSION) {
                root.putInt(SCHEMA_VERSION_TAG, SCHEMA_VERSION);
            }
            return root;
        }
        if (!create) {
            return null;
        }
        root = new CompoundTag();
        root.putInt(SCHEMA_VERSION_TAG, SCHEMA_VERSION);
        itemTag.put(ROOT_TAG, root);
        return root;
    }
}
