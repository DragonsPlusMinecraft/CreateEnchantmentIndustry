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

package plus.dragons.createenchantmentindustry.common.processing;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerMode;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class EnchantmentProcessingRules {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnchantmentProcessingRules.class);
    private static final ResourceLocation RULES_ID = CEIDataMaps.ENCHANTMENT_PROCESSING_RULES.id();
    private static boolean warnedLegacyLevelExtension;
    private static boolean warnedLegacyForgingCost;
    private static boolean warnedLegacySplittingCost;

    public static void warnLegacyDataMaps(MinecraftServer server) {
        var enchantments = server.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        int levelExtensions = 0;
        int forgingCosts = 0;
        int splittingCosts = 0;
        for (var enchantment : enchantments) {
            if (CEIDataMaps.SUPER_ENCHANTING_LEVEL_EXTENSION.get(enchantment) != null)
                levelExtensions++;
            if (CEIDataMaps.FORGING_COST_MULTIPLIER.get(enchantment) != null)
                forgingCosts++;
            if (CEIDataMaps.SPLITTING_COST_MULTIPLIER.get(enchantment) != null)
                splittingCosts++;
        }
        if (levelExtensions > 0)
            warnLegacyLevelExtension(levelExtensions);
        if (forgingCosts > 0)
            warnLegacyForgingCost(forgingCosts);
        if (splittingCosts > 0)
            warnLegacySplittingCost(splittingCosts);
    }

    public static int blazeEnchanterLevelExtension(Enchantment enchantment) {
        var rule = CEIDataMaps.ENCHANTMENT_PROCESSING_RULES.get(enchantment);
        if (rule != null) {
            var value = rule.levelExtension().blazeEnchanter();
            if (value.isPresent())
                return value.get();
        }
        Integer legacy = legacyLevelExtension(enchantment);
        return legacy != null ? legacy : CEIConfig.enchantments().blazeEnchanterMaxLevelExtension.get();
    }

    public static int blazeForgerLevelExtension(Enchantment enchantment) {
        var rule = CEIDataMaps.ENCHANTMENT_PROCESSING_RULES.get(enchantment);
        if (rule != null) {
            var value = rule.levelExtension().blazeForger();
            if (value.isPresent())
                return value.get();
        }
        Integer legacy = legacyLevelExtension(enchantment);
        return legacy != null ? legacy : CEIConfig.enchantments().blazeForgerMaxLevelExtension.get();
    }

    public static float blazeEnchanterCostMultiplier(Enchantment enchantment, boolean special, boolean template) {
        float multiplier = special
                ? CEIConfig.processing().blazeEnchanterSuperEnchantingCostMultiplier.getF()
                : CEIConfig.processing().blazeEnchanterNormalEnchantingCostMultiplier.getF();
        multiplier *= template
                ? CEIConfig.processing().blazeEnchanterTemplateEnchantingCostMultiplier.getF()
                : CEIConfig.processing().blazeEnchanterDirectEnchantingCostMultiplier.getF();

        var rule = CEIDataMaps.ENCHANTMENT_PROCESSING_RULES.get(enchantment);
        if (rule == null)
            return multiplier;
        var multipliers = rule.costMultiplier().blazeEnchanter();
        multiplier *= (special ? multipliers.super_() : multipliers.normal()).orElse(1F);
        multiplier *= (template ? multipliers.template() : multipliers.direct()).orElse(1F);
        return multiplier;
    }

    public static int blazeEnchanterCost(int baseCost, List<EnchantmentInstance> selectedEnchantments, boolean special, boolean template) {
        if (baseCost <= 0 || selectedEnchantments.isEmpty())
            return 0;
        float multiplier = weightedBlazeEnchanterCostMultiplier(selectedEnchantments, special, template);
        return roundCost(baseCost * multiplier);
    }

    public static float weightedBlazeEnchanterCostMultiplier(List<EnchantmentInstance> enchantments, boolean special, boolean template) {
        float weightedMultiplier = 0;
        int totalWeight = 0;
        for (var instance : enchantments) {
            int weight = CEIEnchantmentHelper.anvilCost(instance.enchantment) * Math.max(1, instance.level);
            totalWeight += weight;
            weightedMultiplier += weight * blazeEnchanterCostMultiplier(instance.enchantment, special, template);
        }
        return totalWeight == 0 ? 1 : weightedMultiplier / totalWeight;
    }

    public static int blazeForgerLevelCost(Enchantment enchantment, BlazeForgerMode mode, boolean special, int anvilCost, int level) {
        float cost = Math.max(1, anvilCost / 2) * Math.max(1, level);
        cost *= blazeForgerCostMultiplier(enchantment, mode, special);
        return roundCost(cost);
    }

    public static float blazeForgerCostMultiplier(Enchantment enchantment, BlazeForgerMode mode, boolean special) {
        float multiplier = special
                ? CEIConfig.processing().blazeForgerSuperForgingCostMultiplier.getF()
                : CEIConfig.processing().blazeForgerNormalForgingCostMultiplier.getF();
        multiplier *= switch (mode) {
            case MERGE -> CEIConfig.processing().blazeForgerMergeCostMultiplier.getF();
            case APPLY -> CEIConfig.processing().blazeForgerApplyCostMultiplier.getF();
            case EXTRACT -> CEIConfig.processing().blazeForgerExtractCostMultiplier.getF();
        };

        var rule = CEIDataMaps.ENCHANTMENT_PROCESSING_RULES.get(enchantment);
        if (rule != null) {
            var multipliers = rule.costMultiplier().blazeForger();
            multiplier *= (special ? multipliers.super_() : multipliers.normal()).orElse(1F);
            var operationMultiplier = switch (mode) {
                case MERGE -> multipliers.merge();
                case APPLY -> multipliers.apply();
                case EXTRACT -> multipliers.extract();
            };
            if (operationMultiplier.isPresent())
                return multiplier * operationMultiplier.get();
        }

        Float legacy = legacyForgerCost(enchantment, mode);
        return legacy != null ? multiplier * legacy : multiplier;
    }

    public static int conflictExtraLevelCost() {
        return CEIConfig.processing().blazeForgerConflictExtraLevelCost.get();
    }

    public static int durabilityRepairLevelCost() {
        return CEIConfig.processing().blazeForgerDurabilityRepairLevelCost.get();
    }

    public static int roundCost(float cost) {
        return Math.max(1, Math.round(cost));
    }

    private static Integer legacyLevelExtension(Enchantment enchantment) {
        Integer legacy = CEIDataMaps.SUPER_ENCHANTING_LEVEL_EXTENSION.get(enchantment);
        if (legacy != null) {
            warnLegacyLevelExtension(1);
            // TODO Remove this legacy data map fallback after modpacks have migrated to enchantment_processing/rules.
            return legacy;
        }
        return null;
    }

    private static Float legacyForgerCost(Enchantment enchantment, BlazeForgerMode mode) {
        if (mode == BlazeForgerMode.EXTRACT) {
            Float legacy = CEIDataMaps.SPLITTING_COST_MULTIPLIER.get(enchantment);
            if (legacy != null) {
                warnLegacySplittingCost(1);
                // TODO Remove this legacy data map fallback after modpacks have migrated to enchantment_processing/rules.
                return legacy;
            }
        } else {
            Float legacy = CEIDataMaps.FORGING_COST_MULTIPLIER.get(enchantment);
            if (legacy != null) {
                warnLegacyForgingCost(1);
                // TODO Remove this legacy data map fallback after modpacks have migrated to enchantment_processing/rules.
                return legacy;
            }
        }
        return null;
    }

    private static void warnLegacyLevelExtension(int count) {
        if (warnedLegacyLevelExtension)
            return;
        warnedLegacyLevelExtension = true;
        LOGGER.warn("Deprecated data map {} is still loaded with {} entries. It works through a compatibility fallback, but modpacks should migrate to {}.",
                CEIDataMaps.SUPER_ENCHANTING_LEVEL_EXTENSION.id(), count, RULES_ID);
    }

    private static void warnLegacyForgingCost(int count) {
        if (warnedLegacyForgingCost)
            return;
        warnedLegacyForgingCost = true;
        LOGGER.warn("Deprecated data map {} is still loaded with {} entries. It works through a compatibility fallback, but modpacks should migrate to {}.",
                CEIDataMaps.FORGING_COST_MULTIPLIER.id(), count, RULES_ID);
    }

    private static void warnLegacySplittingCost(int count) {
        if (warnedLegacySplittingCost)
            return;
        warnedLegacySplittingCost = true;
        LOGGER.warn("Deprecated data map {} is still loaded with {} entries. It works through a compatibility fallback, but modpacks should migrate to {}.",
                CEIDataMaps.SPLITTING_COST_MULTIPLIER.id(), count, RULES_ID);
    }
}
