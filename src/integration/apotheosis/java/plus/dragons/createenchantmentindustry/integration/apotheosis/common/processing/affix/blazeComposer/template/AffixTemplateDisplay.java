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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AffixTemplateDisplay {
    public static List<Component> describeStack(ItemStack stack) {
        AffixTemplateData data = AffixTemplateOps.getTemplateData(stack);
        if (data != null && data.isBound()) {
            List<Component> result = new ArrayList<>();
            result.add(describeTemplate(data, stack));
            if (data.size() > 1)
                describeTemplateEntries(data, stack).forEach(result::add);
            return result;
        }
        List<Component> result = new ArrayList<>();
        result.add(stack.getHoverName().copy());
        AffixHelper.getAffixes(stack).values().stream()
                .filter(AffixInstance::isValid)
                .sorted(Comparator.comparing(instance -> instance.affix().getId()))
                .map(AffixTemplateDisplay::describeAffix)
                .forEach(result::add);
        return result;
    }

    public static Component describeTemplate(AffixTemplateData data, ItemStack stack) {
        if (data.size() == 1)
            return describeTemplateEntry(data, data.entries().get(0), stack);
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.template_affixes",
                data.size(),
                rarityName(data));
    }

    public static List<Component> describeTemplateEntries(AffixTemplateData data, ItemStack stack) {
        return data.entries().stream()
                .map(entry -> describeTemplateEntry(data, entry, stack))
                .toList();
    }

    public static Component describeTemplateEntry(AffixTemplateData data, AffixTemplateEntry entry, ItemStack stack) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.template_affix",
                affixName(entry, data.rarity(), stack),
                formatLevel(entry.level()),
                rarityName(data));
    }

    public static Component describeTemplateEntryRange(AffixTemplateData data, AffixTemplateEntry entry, float minLevel, float maxLevel, ItemStack stack) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.template_affix_range",
                affixName(entry, data.rarity(), stack),
                formatLevel(minLevel),
                formatLevel(maxLevel),
                rarityName(data));
    }

    public static Component describeTemplateEntryUpgrade(AffixTemplateData data, AffixTemplateEntry before, AffixTemplateEntry after, ItemStack stack) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.template_affix_upgrade",
                affixName(after, data.rarity(), stack),
                formatLevel(before.level()),
                formatLevel(after.level()),
                rarityName(data));
    }

    public static Component describeTemplateEntryUpgradeRange(AffixTemplateData data, AffixTemplateEntry before, AffixTemplateEntry after, float minLevel, float maxLevel, ItemStack stack) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.template_affix_upgrade_range",
                affixName(after, data.rarity(), stack),
                formatLevel(before.level()),
                formatLevel(minLevel),
                formatLevel(maxLevel),
                rarityName(data));
    }

    public static Component describeEquipmentAffix(ItemStack stack, AffixInstance instance) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.equipment_affix",
                stack.getHoverName().copy(),
                affixName(instance),
                formatLevel(instance.level()));
    }

    public static Component describeEquipmentAffixUpgrade(ItemStack stack, DynamicHolder<? extends Affix> affix, float before, float after) {
        AffixInstance instance = AffixHelper.getAffixes(stack).get(affix);
        if (instance == null) {
            instance = new AffixInstance(affix, stack, AffixHelper.getRarity(stack), after);
        }
        if (before <= 0) {
            return Component.translatable(
                    "create_enchantment_industry.gui.goggles.blaze_composer.result.equipment_affix",
                    stack.getHoverName().copy(),
                    affixName(instance),
                    formatLevel(after));
        }
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.equipment_affix_upgrade",
                stack.getHoverName().copy(),
                affixName(instance),
                formatLevel(before),
                formatLevel(after));
    }

    public static Component describeEquipmentAffixUpgradeRange(ItemStack stack, DynamicHolder<? extends Affix> affix, float before, float minAfter, float maxAfter) {
        AffixInstance instance = AffixHelper.getAffixes(stack).get(affix);
        if (instance == null) {
            instance = new AffixInstance(affix, stack, AffixHelper.getRarity(stack), maxAfter);
        }
        if (before <= 0) {
            return Component.translatable(
                    "create_enchantment_industry.gui.goggles.blaze_composer.result.equipment_affix_range",
                    stack.getHoverName().copy(),
                    affixName(instance),
                    formatLevel(minAfter),
                    formatLevel(maxAfter));
        }
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.equipment_affix_upgrade_range",
                stack.getHoverName().copy(),
                affixName(instance),
                formatLevel(before),
                formatLevel(minAfter),
                formatLevel(maxAfter));
    }

    public static Component describeRemovedAffix(ItemStack stack, AffixInstance instance) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.equipment_removed_affix",
                stack.getHoverName().copy(),
                affixName(instance),
                formatLevel(instance.level()));
    }

    public static Component describeLostEntry(AffixTemplateData data, AffixTemplateEntry entry, ItemStack stack, Component reason) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.lost_affix",
                describeAffix(data, entry, stack),
                reason);
    }

    public static Component describeRejectedEntry(AffixTemplateData data, AffixTemplateEntry entry, ItemStack stack, Component reason) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.rejected_affix",
                describeAffix(data, entry, stack),
                reason);
    }

    public static Component describeAffix(AffixTemplateData data, AffixTemplateEntry entry, ItemStack stack) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.affix",
                affixName(entry, data.rarity(), stack),
                formatLevel(entry.level()));
    }

    public static Component describeAffix(AffixInstance instance) {
        return Component.translatable(
                "create_enchantment_industry.gui.goggles.blaze_composer.result.affix",
                affixName(instance),
                formatLevel(instance.level()));
    }

    public static MutableComponent affixName(AffixTemplateEntry entry, DynamicHolder<LootRarity> rarity, ItemStack stack) {
        if (!entry.isBound()) {
            return Component.literal(entry.affix().getId().toString()).withStyle(ChatFormatting.RED);
        }
        return affixName(entry.toInstance(rarity, stack));
    }

    public static MutableComponent affixName(AffixInstance instance) {
        if (!instance.affix().isBound()) {
            return Component.literal(instance.affix().getId().toString()).withStyle(ChatFormatting.RED);
        }
        MutableComponent name = Component.empty().append(instance.getName(true));
        if (instance.rarity().isBound()) {
            name.withStyle(style -> style.withColor(instance.rarity().get().getColor()));
        } else {
            name.withStyle(ChatFormatting.GRAY);
        }
        return name;
    }

    public static Component rarityName(AffixTemplateData data) {
        return rarityName(data.rarity());
    }

    public static Component rarityName(DynamicHolder<LootRarity> rarity) {
        if (!rarity.isBound())
            return Component.literal(rarity.getId().toString()).withStyle(ChatFormatting.RED);
        return rarity.get().toComponent().copy().withStyle(style -> style.withColor(rarity.get().getColor()));
    }

    public static Component sourceCategoryName(ResourceLocation category) {
        return Component.translatable(category.toLanguageKey("loot_category"));
    }

    public static String formatLevel(float level) {
        if (level == (int) level) {
            return Integer.toString((int) level);
        }
        return String.format(Locale.ROOT, "%.2f", level);
    }
}
