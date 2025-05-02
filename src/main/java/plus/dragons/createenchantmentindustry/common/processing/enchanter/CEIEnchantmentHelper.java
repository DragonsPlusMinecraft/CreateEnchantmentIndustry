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

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.CommonHooks;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class CEIEnchantmentHelper {
    public static int getEnchantmentCost(Holder<Enchantment> holder, int level) {
        var enchantment = holder.value();
        int cost = ExperienceHelper.getExperienceForNextLevel(enchantment.getMinCost(level));
        if (level == 1)
            return cost;
        return cost + getEnchantmentCost(holder, level - 1);
    }

    public static int getEnchantmentCost(ItemEnchantments enchantments) {
        return enchantments.entrySet().stream()
                .mapToInt(entry -> getEnchantmentCost(entry.getKey(), entry.getIntValue()))
                .sum();
    }

    public static int getAdjustedLevel(ItemStack stack, int level) {
        var value = stack.getEnchantmentValue();
        if (value > 0)
            level += 1 + value / 4;
        float f = 0.15F;
        level = Mth.clamp(Math.round(level + level * f), 1, Integer.MAX_VALUE);
        return level;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int level,
            Stream<Holder<Enchantment>> possibleEnchantments, boolean special) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        possibleEnchantments.forEach(holder -> {
            Enchantment enchantment = holder.value();
            int maxLevel = enchantment.getMaxLevel();
            // Remove since Blaze Enchanter should not break level cap
            /*
             * if (maxLevel > 1 && special)
             * maxLevel += CEIConfig.enchantments().enchantmentMaxLevelExtension.get();
             * maxLevel = Math.clamp(maxLevel, 1, 255);
             */
            for (int i = maxLevel; i >= enchantment.getMinLevel(); i--) {
                if (level >= enchantment.getMinCost(i) && level <= enchantment.getMaxCost(i)) {
                    list.add(new EnchantmentInstance(holder, i));
                    break;
                }
            }
        });
        return list;
    }

    public static List<EnchantmentInstance> selectEnchantments(RandomSource random, int adjustedLevel,
            List<EnchantmentInstance> available, boolean special) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        WeightedRandom.getRandomItem(random, available).ifPresent(list::add);
        while (random.nextInt(50) <= adjustedLevel) {
            if (!list.isEmpty())
                if (special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get()) {
                    available.removeIf(instance -> instance.enchantment.equals(list.getLast().enchantment));
                } else {
                    EnchantmentHelper.filterCompatibleEnchantments(available, list.getLast());
                }
            if (available.isEmpty())
                break;
            WeightedRandom.getRandomItem(random, available).ifPresent(list::add);
            adjustedLevel /= 2;
        }
        return list;
    }

    public static boolean hasTag(ItemStack stack, TagKey<Enchantment> tag) {
        ItemEnchantments itemenchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (itemenchantments.isEmpty()) {
            return false;
        }

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
            if (entry.getKey().is(tag)) {
                return true;
            }
        }
        return false;
    }
}
