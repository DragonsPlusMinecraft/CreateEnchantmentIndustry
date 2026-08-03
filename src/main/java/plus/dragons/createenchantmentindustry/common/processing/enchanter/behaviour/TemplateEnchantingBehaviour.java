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

package plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour;

import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;

public class TemplateEnchantingBehaviour extends EnchantingBehaviour {
    private final ItemStack target;

    public TemplateEnchantingBehaviour(ItemStack target) {
        this.target = target;
    }

    @Override
    public boolean canProcess(Level level, ItemStack stack, boolean special) {
        if (enchantments.isEmpty())
            return false;
        if (stack.getItem() instanceof EnchantingTemplateItem template) {
            if (CEIItemData.getStoredEnchantments(stack).isEmpty())
                return (!special && !template.isSpecial()) || (special && template.isSpecial());
        }
        return false;
    }

    @Override
    public void update(Level level, ItemStack stack, int enchantingLevel, boolean special, boolean cursed) {
        super.update(level, target, enchantingLevel, special, cursed);
    }

    @Override
    public void update(Level level, ItemStack stack, int enchantingLevel, boolean special, boolean cursed, RandomSource random) {
        super.update(level, target, enchantingLevel, special, cursed, random);
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, RandomSource random, boolean special) {
        var selected = selectResultEnchantments(random, stack, special);
        ItemStack result = stack.copy();
        var enchantments = new LinkedHashMap<net.minecraft.world.item.enchantment.Enchantment, Integer>();
        selected.forEach(instance -> enchantments.put(instance.enchantment, instance.level));
        CEIItemData.setStoredEnchantments(result, enchantments);
        return result;
    }

    @Override
    protected List<EnchantmentInstance> selectResultEnchantments(RandomSource random, ItemStack stack, boolean special) {
        var enchantments = CEIEnchantmentHelper.selectEnchantments(random, enchantingLevel, this.enchantments, special);
        if (enchantments.size() > 1)
            enchantments.remove(random.nextInt(enchantments.size()));
        applyCursePenalty(enchantments, random, special);
        return enchantments;
    }
}
