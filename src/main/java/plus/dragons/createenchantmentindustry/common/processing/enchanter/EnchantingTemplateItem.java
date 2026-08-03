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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;

public class EnchantingTemplateItem extends Item {
    private final boolean special;

    public EnchantingTemplateItem(Properties properties, boolean special) {
        super(properties);
        this.special = special;
    }

    public static EnchantingTemplateItem normal(Properties properties) {
        return new EnchantingTemplateItem(properties, false);
    }

    public static EnchantingTemplateItem special(Properties properties) {
        return new EnchantingTemplateItem(properties, true);
    }

    public boolean isSpecial() {
        return special;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !CEIItemData.getStoredEnchantments(stack).isEmpty();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
