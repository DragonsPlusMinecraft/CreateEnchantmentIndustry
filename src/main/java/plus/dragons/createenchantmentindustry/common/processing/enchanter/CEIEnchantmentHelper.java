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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRules;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class CEIEnchantmentHelper {
    @Nullable
    public static Function<Enchantment, Integer> alternativeMaxLevel;

    /**
     * Compatibility equivalent of 1.21.1's primary-item predicate.
     */
    public static boolean isPrimaryItemFor(ItemStack stack, Enchantment enchantment) {
        if (stack.is(Items.BOOK))
            return true;
        if (stack.getItem() instanceof EnchantingTemplateItem)
            return false;
        return enchantment.canApplyAtEnchantingTable(stack);
    }

    /**
     * Compatibility equivalent of 1.21.1's general item-support predicate.
     */
    public static boolean supportsEnchantment(ItemStack stack, Enchantment enchantment) {
        if (stack.is(Items.ENCHANTED_BOOK))
            return true;
        return enchantment.canEnchant(stack);
    }

    public static int getEnchantmentCost(Enchantment enchantment, int level) {
        int cost = ExperienceHelper.getExperienceForNextLevel(enchantment.getMinCost(level));
        if (level == 1)
            return cost;
        return cost + getEnchantmentCost(enchantment, level - 1);
    }

    public static int getEnchantmentCost(Map<Enchantment, Integer> enchantments) {
        return enchantments.entrySet().stream()
                .mapToInt(entry -> getEnchantmentCost(entry.getKey(), entry.getValue()))
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

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int level, Stream<Enchantment> possibleEnchantments, boolean special) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        possibleEnchantments.forEach(enchantment -> {
            int maxLevel = maxLevel(enchantment);
            if (special)
                maxLevel += EnchantmentProcessingRules.blazeEnchanterLevelExtension(enchantment);
            for (int i = maxLevel; i >= enchantment.getMinLevel(); i--) {
                if (level >= enchantment.getMinCost(i) && level <= enchantment.getMaxCost(i)) {
                    list.add(new EnchantmentInstance(enchantment, i));
                    break;
                }
            }
        });
        return list;
    }

    public static List<EnchantmentInstance> getAvailablePenaltyCurseResults(Stream<Enchantment> possibleEnchantments, int maxPenaltyLevel) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        if (maxPenaltyLevel <= 0)
            return list;
        possibleEnchantments.forEach(enchantment -> {
            int level = Math.min(maxLevel(enchantment), maxPenaltyLevel);
            if (level >= enchantment.getMinLevel())
                list.add(new EnchantmentInstance(enchantment, level));
        });
        return list;
    }

    public static List<EnchantmentInstance> selectEnchantments(RandomSource random, int adjustedLevel, List<EnchantmentInstance> available, boolean special) {
        available = Lists.newArrayList(available);
        List<EnchantmentInstance> list = Lists.newArrayList();
        WeightedRandom.getRandomItem(random, available).ifPresent(list::add);
        while (random.nextInt(50) <= adjustedLevel) {
            if (!list.isEmpty())
                if (special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get()) {
                    available.removeIf(instance -> instance.enchantment.equals(list.get(list.size() - 1).enchantment));
                } else {
                    EnchantmentHelper.filterCompatibleEnchantments(available, list.get(list.size() - 1));
                }
            if (available.isEmpty())
                break;
            WeightedRandom.getRandomItem(random, available).ifPresent(list::add);
            adjustedLevel /= 2;
        }
        return list;
    }

    public static int maxLevel(Enchantment enchantment) {
        if (alternativeMaxLevel == null) return enchantment.getMaxLevel();
        return alternativeMaxLevel.apply(enchantment);
    }

    public static int anvilCost(Enchantment enchantment) {
        return switch (enchantment.getRarity()) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 4;
            case VERY_RARE -> 8;
        };
    }

    @Deprecated(forRemoval = false)
    public static int levelExtension(Enchantment enchantment) {
        // Legacy ABI entry point. New code should call the machine-specific rule helpers directly.
        return EnchantmentProcessingRules.blazeForgerLevelExtension(enchantment);
    }
}
