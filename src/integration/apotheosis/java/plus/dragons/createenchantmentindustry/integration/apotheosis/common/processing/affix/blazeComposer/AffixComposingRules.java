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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateEntry;

public class AffixComposingRules extends SimplePreparableReloadListener<AffixComposingRules.LoadedRules> {
    public static final AffixComposingRules INSTANCE = new AffixComposingRules();
    private static final Logger LOGGER = LoggerFactory.getLogger(AffixComposingRules.class);
    private static final Gson GSON = new Gson();
    private static final String AFFIX_DIRECTORY = "create_enchantment_industry/affix_composing/affix";
    private static final String RARITY_DIRECTORY = "create_enchantment_industry/affix_composing/rarity";

    private volatile LoadedRules rules = LoadedRules.EMPTY;

    private AffixComposingRules() {}

    @Override
    protected LoadedRules prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> affixObjects = new HashMap<>();
        Map<ResourceLocation, JsonElement> rarityObjects = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, AFFIX_DIRECTORY, GSON, affixObjects);
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, RARITY_DIRECTORY, GSON, rarityObjects);
        return new LoadedRules(
                parseRules("affix", affixObjects),
                parseRules("rarity", rarityObjects));
    }

    @Override
    protected void apply(LoadedRules loadedRules, ResourceManager resourceManager, ProfilerFiller profiler) {
        rules = loadedRules;
        LOGGER.debug("Loaded {} affix-targeted composing rules and {} rarity-targeted composing rules", loadedRules.affixes().size(), loadedRules.rarities().size());
    }

    public float getCostMultiplier(AffixTemplateEntry entry, DynamicHolder<LootRarity> rarity) {
        LoadedRules current = rules;
        return current.affix(entry.affix()).costMultiplier()
                * current.rarity(rarity.getId()).costMultiplier();
    }

    public float getAugmentingCostMultiplier(AffixInstance instance) {
        LoadedRules current = rules;
        AffixComposingRule affixRule = current.affix(instance.affix());
        AffixComposingRule rarityRule = current.rarity(instance.rarity().getId());
        return affixRule.costMultiplier()
                * affixRule.augmentingCostMultiplier()
                * rarityRule.costMultiplier()
                * rarityRule.augmentingCostMultiplier();
    }

    public float getMaxLevel(AffixTemplateEntry entry, DynamicHolder<LootRarity> rarity, float templateMaxLevel) {
        LoadedRules current = rules;
        float maxLevel = templateMaxLevel;
        AffixComposingRule affixRule = current.affix(entry.affix());
        AffixComposingRule rarityRule = current.rarity(rarity.getId());
        if (affixRule.maxLevel().isPresent())
            maxLevel = Math.min(maxLevel, affixRule.maxLevel().get());
        if (rarityRule.maxLevel().isPresent())
            maxLevel = Math.min(maxLevel, rarityRule.maxLevel().get());
        return maxLevel;
    }

    public boolean denies(BlazeComposerMode mode, boolean superMode, AffixTemplateEntry entry, DynamicHolder<LootRarity> rarity) {
        LoadedRules current = rules;
        AffixComposingRule affixRule = current.affix(entry.affix());
        AffixComposingRule rarityRule = current.rarity(rarity.getId());
        return affixRule.denies(mode, superMode) || rarityRule.denies(mode, superMode);
    }

    public boolean deniesAugmenting(AffixInstance instance) {
        LoadedRules current = rules;
        AffixComposingRule affixRule = current.affix(instance.affix());
        AffixComposingRule rarityRule = current.rarity(instance.rarity().getId());
        return affixRule.denyAugmenting() || rarityRule.denyAugmenting();
    }

    private static Map<ResourceLocation, AffixComposingRule> parseRules(
            String targetType,
            Map<ResourceLocation, JsonElement> objects) {
        Map<ResourceLocation, AffixComposingRule> parsed = new HashMap<>();
        objects.forEach((id, element) -> AffixComposingRule.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(message -> LOGGER.error("Failed to parse affix composing {} rule {}: {}", targetType, id, message))
                .ifPresent(rule -> parsed.put(id, rule)));
        return Map.copyOf(parsed);
    }

    protected record LoadedRules(
            Map<ResourceLocation, AffixComposingRule> affixes,
            Map<ResourceLocation, AffixComposingRule> rarities) {
        private static final LoadedRules EMPTY = new LoadedRules(Map.of(), Map.of());

        public LoadedRules {
            affixes = Map.copyOf(affixes);
            rarities = Map.copyOf(rarities);
        }

        private AffixComposingRule affix(DynamicHolder<? extends Affix> affix) {
            return affixes.getOrDefault(affix.getId(), AffixComposingRule.DEFAULT);
        }

        private AffixComposingRule rarity(ResourceLocation rarity) {
            return rarities.getOrDefault(rarity, AffixComposingRule.DEFAULT);
        }
    }
}
