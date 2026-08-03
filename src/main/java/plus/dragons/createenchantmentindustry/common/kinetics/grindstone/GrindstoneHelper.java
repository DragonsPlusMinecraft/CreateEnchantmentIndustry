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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GrindstoneEvent;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;

public class GrindstoneHelper {
    public static boolean canItemBeGrinded(ItemStack top, ItemStack bottom) {
        var event = new GrindstoneEvent.OnPlaceItem(top, bottom, -1);
        if (MinecraftForge.EVENT_BUS.post(event))
            return false;
        if (!event.getOutput().isEmpty())
            return true;
        return !computeResult(top, bottom).isEmpty();
    }

    public static Optional<Result> grindItem(Level level, ItemStack top, ItemStack bottom) {
        var place = new GrindstoneEvent.OnPlaceItem(top, bottom, -1);
        if (MinecraftForge.EVENT_BUS.post(place))
            return Optional.empty();
        int experience = place.getXp();
        var output = place.getOutput();
        if (output.isEmpty()) {
            output = computeResult(top, bottom);
            if (output.isEmpty())
                return Optional.empty();
            if (experience == -1) {
                experience = getGrindingExperience(level, top, bottom);
            }
        }
        var take = new GrindstoneEvent.OnTakeItem(top, bottom, experience);
        if (MinecraftForge.EVENT_BUS.post(take))
            return Optional.empty();
        return Optional.of(new Result(take.getNewTopItem(), take.getNewBottomItem(), output, Math.max(take.getXp(), 0)));
    }

    private static int getGrindingExperience(Level level, ItemStack top, ItemStack bottom) {
        int experience = 0;
        experience += getExperienceFromItem(top);
        experience += getExperienceFromItem(bottom);
        if (experience > 0) {
            int average = Mth.ceil(experience / 2.0);
            return average + level.random.nextInt(average);
        } else {
            return 0;
        }
    }

    public static int getExperienceFromItem(ItemStack stack) {
        int result = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (var entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            if (!enchantment.isCurse()) {
                result += enchantment.getMinCost(level);
            }
        }
        return result;
    }

    public static int getExperienceFromGrindingRecipe(Level level, ItemStack stack) {
        var input = new SimpleContainer(stack);
        var grinding = SequencedAssemblyRecipe.getRecipe(level, input, CEIRecipes.GRINDING.getType(), GrindingRecipe.class);
        if (grinding.isEmpty())
            grinding = level.getRecipeManager().getRecipeFor(CEIRecipes.GRINDING.getType(), input, level);
        if (grinding.isEmpty()) return 0;
        var f = grinding.get().getFluidResults();
        if (f.isEmpty()) return 0;
        return f.get(0).getAmount();
    }

    private static ItemStack computeResult(ItemStack top, ItemStack bottom) {
        boolean topEmpty = top.isEmpty();
        boolean bottomEmpty = bottom.isEmpty();
        if (topEmpty && bottomEmpty) {
            return ItemStack.EMPTY;
        } else if (top.getCount() <= 1 && bottom.getCount() <= 1) {
            if (topEmpty || bottomEmpty) {
                ItemStack input = topEmpty ? bottom : top;
                return EnchantmentHelper.getEnchantments(input).isEmpty()
                        ? ItemStack.EMPTY
                        : removeNonCursesFrom(input.copy());
            } else {
                return mergeItems(top, bottom);
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack mergeItems(ItemStack top, ItemStack bottom) {
        if (!top.is(bottom.getItem())) {
            return ItemStack.EMPTY;
        } else {
            int maxDamage = Math.max(top.getMaxDamage(), bottom.getMaxDamage());
            int topDurability = top.getMaxDamage() - top.getDamageValue();
            int bottomDurability = bottom.getMaxDamage() - bottom.getDamageValue();
            int l = topDurability + bottomDurability + maxDamage * 5 / 100;
            int count = 1;
            if (!top.isDamageableItem() || !top.isRepairable()) {
                if (top.getMaxStackSize() < 2 || !ItemStack.matches(top, bottom)) {
                    return ItemStack.EMPTY;
                }

                count = 2;
            }

            ItemStack result = top.copy();
            result.setCount(count);
            if (result.isDamageableItem()) {
                result.setDamageValue(Math.max(maxDamage - l, 0));
                if (!bottom.isRepairable())
                    result.setDamageValue(top.getDamageValue());
            }

            mergeEnchantsFrom(result, bottom);
            return removeNonCursesFrom(result);
        }
    }

    private static void mergeEnchantsFrom(ItemStack top, ItemStack bottom) {
        Map<Enchantment, Integer> topEnchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(top));
        for (var entry : EnchantmentHelper.getEnchantments(bottom).entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.isCurse() || !topEnchantments.containsKey(enchantment))
                topEnchantments.merge(enchantment, entry.getValue(), Math::max);
        }
        CEIItemData.setEnchantments(top, topEnchantments);
    }

    public static ItemStack removeNonCursesFrom(ItemStack input) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(input));
        enchantments.keySet().removeIf(enchantment -> !enchantment.isCurse());
        CEIItemData.setEnchantments(input, enchantments);
        if (input.is(Items.ENCHANTED_BOOK) && enchantments.isEmpty()) {
            input = CEIItemData.transmuteCopy(input, Items.BOOK);
        }

        int repairCost = 0;

        for (int j = 0; j < enchantments.size(); j++) {
            repairCost = AnvilMenu.calculateIncreasedRepairCost(repairCost);
        }

        CEIItemData.setRepairCost(input, repairCost);
        return input;
    }

    public record Result(ItemStack top, ItemStack bottom, ItemStack output, int experience) {}
}
