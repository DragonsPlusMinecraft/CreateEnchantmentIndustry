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

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRules;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class EnchantingBehaviour {
    protected int enchantingLevel;
    protected boolean cursed;
    protected TagKey<Enchantment> enchantmentTag = CEIEnchantments.MOD_TAGS.enchanting;
    protected List<EnchantmentInstance> enchantments = new ArrayList<>(0);
    protected List<EnchantmentInstance> penaltyCurses = new ArrayList<>(0);
    protected List<EnchantmentInstance> costEnchantments = new ArrayList<>(0);

    protected List<EnchantmentInstance> getAvailableEnchantments(Level level, ItemStack stack, boolean special) {
        int adjustedLevel = CEIEnchantmentHelper.getAdjustedLevel(stack, enchantingLevel);
        if (adjustedLevel == 0)
            return new ArrayList<>(0);
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var possible = registry.getTag(enchantmentTag).stream()
                .flatMap(HolderSet::stream)
                .filter(holder -> !special || !holder.is(CEIEnchantments.MOD_TAGS.enchantingExclusive))
                .map(Holder::value)
                .filter(enchantment -> CEIEnchantmentHelper.isPrimaryItemFor(stack, enchantment));
        return CEIEnchantmentHelper.getAvailableEnchantmentResults(adjustedLevel, possible, special);
    }

    protected List<EnchantmentInstance> getAvailablePenaltyCurses(Level level, ItemStack stack) {
        if (CEIConfig.enchantments().blazeEnchanterBlockedLightningCurseChance.get() <= 0)
            return new ArrayList<>(0);
        if (CEIConfig.enchantments().blazeEnchanterBlockedLightningCurseCount.get() <= 0)
            return new ArrayList<>(0);
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var possible = registry.getTag(CEIEnchantments.MOD_TAGS.penaltyCurses).stream()
                .flatMap(HolderSet::stream)
                .filter(enchantment -> !enchantment.is(CEIEnchantments.MOD_TAGS.penaltyCursesDeny))
                .map(Holder::value)
                .filter(Enchantment::isCurse)
                .filter(enchantment -> stack.is(Items.BOOK)
                        || CEIEnchantmentHelper.supportsEnchantment(stack, enchantment));
        return CEIEnchantmentHelper.getAvailablePenaltyCurseResults(
                possible,
                CEIConfig.enchantments().blazeEnchanterBlockedLightningCurseMaxLevel.get());
    }

    public boolean canProcess(Level level, ItemStack stack, boolean special) {
        if (stack.getItem() instanceof EnchantingTemplateItem)
            return false;
        return stack.isEnchantable() && !getAvailableEnchantments(level, stack, special).isEmpty();
    }

    public void update(Level level, ItemStack stack, int enchantingLevel, boolean special, boolean cursed) {
        update(level, stack, enchantingLevel, special, cursed, null);
    }

    public void update(Level level, ItemStack stack, int enchantingLevel, boolean special, boolean cursed, RandomSource random) {
        this.enchantingLevel = enchantingLevel;
        this.cursed = special && cursed;
        enchantmentTag = special
                ? CEIEnchantments.MOD_TAGS.superEnchanting
                : CEIEnchantments.MOD_TAGS.enchanting;
        enchantments = getAvailableEnchantments(level, stack, special);
        penaltyCurses = !enchantments.isEmpty() && this.cursed
                ? getAvailablePenaltyCurses(level, stack)
                : new ArrayList<>(0);
        costEnchantments = random == null || enchantments.isEmpty()
                ? new ArrayList<>(0)
                : selectResultEnchantments(random, stack, special);
    }

    public ItemStack getResult(Level level, ItemStack stack, RandomSource random, boolean special) {
        var selected = selectResultEnchantments(random, stack, special);
        ItemStack result = stack.is(Items.BOOK)
                ? CEIItemData.transmuteCopy(stack, Items.ENCHANTED_BOOK)
                : stack.copy();
        var enchantments = new java.util.LinkedHashMap<>(CEIItemData.getEnchantments(result));
        selected.forEach(instance -> enchantments.merge(instance.enchantment, instance.level, Math::max));
        CEIItemData.setEnchantments(result, enchantments);
        return result;
    }

    protected List<EnchantmentInstance> selectResultEnchantments(RandomSource random, ItemStack stack, boolean special) {
        int adjustedLevel = CEIEnchantmentHelper.getAdjustedLevel(stack, enchantingLevel);
        var enchantments = CEIEnchantmentHelper.selectEnchantments(random, adjustedLevel, this.enchantments, special);
        if (stack.is(Items.BOOK) && enchantments.size() > 1) {
            enchantments.remove(random.nextInt(enchantments.size()));
        }
        applyCursePenalty(enchantments, random, special);
        return enchantments;
    }

    protected void applyCursePenalty(List<EnchantmentInstance> enchantments, RandomSource random, boolean special) {
        if (!special || !cursed || penaltyCurses.isEmpty())
            return;
        double chance = CEIConfig.enchantments().blazeEnchanterBlockedLightningCurseChance.get();
        int count = CEIConfig.enchantments().blazeEnchanterBlockedLightningCurseCount.get();
        if (chance <= 0 || count <= 0)
            return;
        for (int i = 0; i < count; i++) {
            if (random.nextFloat() >= chance)
                continue;
            var available = new ArrayList<>(penaltyCurses);
            removeAlreadySelected(available, enchantments);
            if (!CEIConfig.enchantments().ignoreEnchantmentCompatibility.get()) {
                for (var selected : enchantments) {
                    EnchantmentHelper.filterCompatibleEnchantments(available, selected);
                }
            }
            if (available.isEmpty())
                return;
            WeightedRandom.getRandomItem(random, available)
                    .ifPresent(enchantments::add);
        }
    }

    private void removeAlreadySelected(List<EnchantmentInstance> available, List<EnchantmentInstance> selected) {
        available.removeIf(instance -> selected.stream()
                .anyMatch(enchantment -> enchantment.enchantment.equals(instance.enchantment)));
    }

    public int getExperienceCost() {
        if (enchantments.isEmpty())
            return 0;
        int levelCost = (enchantingLevel + 9) / 10;
        int experienceCost = 0;
        for (int i = 0; i < levelCost; i++) {
            experienceCost += ExperienceHelper.getExperienceForNextLevel(enchantingLevel - i);
        }
        return EnchantmentProcessingRules.blazeEnchanterCost(experienceCost, costEnchantments.isEmpty() ? enchantments : costEnchantments, false, false);
    }

    public int getExperienceCost(boolean special, boolean template) {
        if (enchantments.isEmpty())
            return 0;
        int levelCost = (enchantingLevel + 9) / 10;
        int experienceCost = 0;
        for (int i = 0; i < levelCost; i++) {
            experienceCost += ExperienceHelper.getExperienceForNextLevel(enchantingLevel - i);
        }
        return EnchantmentProcessingRules.blazeEnchanterCost(experienceCost, costEnchantments.isEmpty() ? enchantments : costEnchantments, special, template);
    }

    public List<EnchantmentInstance> getPreviewEnchantments() {
        var preview = new ArrayList<>(enchantments);
        preview.addAll(penaltyCurses);
        return List.copyOf(preview);
    }
}
