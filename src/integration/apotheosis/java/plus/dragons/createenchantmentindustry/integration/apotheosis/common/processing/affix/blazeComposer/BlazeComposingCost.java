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

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.List;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.AffixLevelLimits;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.AffixOperationCosts;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateEntry;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateTier;
import plus.dragons.createenchantmentindustry.integration.apotheosis.config.CEIAXConfig;

public class BlazeComposingCost {
    public static int calculate(BlazeComposerMode mode, AffixTemplateTier tier, DynamicHolder<LootRarity> rarity, List<EntryCost> entries, float extraCost) {
        float cost = baseCost(mode) + Math.max(0, extraCost);
        for (EntryCost entry : entries) {
            cost += entryCost(entry.operation(), tier, rarity, entry.entry(), entry.fromLevel(), entry.resultLevel());
        }
        return AffixOperationCosts.roundCost(cost);
    }

    public static int calculate(Operation operation, BlazeComposerMode mode, AffixTemplateTier tier, DynamicHolder<LootRarity> rarity, AffixTemplateEntry entry, float fromLevel, float resultLevel) {
        return calculate(mode, tier, rarity, List.of(new EntryCost(operation, entry, fromLevel, resultLevel)), 0);
    }

    public static float entryCost(Operation operation, AffixTemplateTier tier, DynamicHolder<LootRarity> rarity, AffixTemplateEntry entry, float fromLevel, float resultLevel) {
        float cost = levelCost(operation, fromLevel, resultLevel);
        cost *= tierMultiplier(tier);
        cost *= AffixOperationCosts.typeMultiplier(entry.affix().get().getType());
        cost *= AffixComposingRules.INSTANCE.getCostMultiplier(entry, rarity);
        return cost;
    }

    public static float exclusiveSetBypassCost(int bypassedConflicts, float multiplier) {
        if (bypassedConflicts <= 0 || multiplier <= 0)
            return 0;
        return AffixOperationCosts.apotheosisUpgradeReferenceCost() * bypassedConflicts * multiplier;
    }

    public static float levelCost(Operation operation, float fromLevel, float resultLevel) {
        var config = CEIAXConfig.server().affixes();
        float stepWeight = Math.max(AffixOperationCosts.EPSILON, AffixOperationCosts.weightedLevelSpan(0, AffixOperationCosts.APOTHEOSIS_AUGMENTING_STEP));
        float standardEnd = Math.min(resultLevel, AffixLevelLimits.EXTENDED_MAX_LEVEL);
        float standardCost = AffixOperationCosts.apotheosisUpgradeReferenceCost()
                * AffixOperationCosts.weightedLevelSpan(fromLevel, standardEnd)
                / stepWeight
                * operation.multiplier();
        if (standardCost > 0) {
            standardCost = Math.min(
                    standardCost,
                    AffixOperationCosts.apotheosisUpgradeReferenceCost() * config.blazeComposerStandardOperationCostCap.getF());
        }
        float superCost = AffixOperationCosts.apotheosisUpgradeReferenceCost()
                * AffixOperationCosts.weightedLevelSpan(Math.max(fromLevel, AffixLevelLimits.EXTENDED_MAX_LEVEL), resultLevel)
                / stepWeight
                * operation.multiplier();
        return standardCost + superCost;
    }

    public static int baseCost(BlazeComposerMode mode) {
        var config = CEIAXConfig.server().affixes();
        return switch (mode) {
            case EXTRACT -> config.blazeComposerExtractBaseCost.get();
            case APPLY -> config.blazeComposerApplyBaseCost.get();
            case MERGE -> config.blazeComposerMergeBaseCost.get();
        };
    }

    public static float tierMultiplier(AffixTemplateTier tier) {
        var config = CEIAXConfig.server().affixes();
        return switch (tier) {
            case BRASS -> config.brassAffixTemplateCostMultiplier.getF();
            case CRYSTAL -> config.crystalAffixTemplateCostMultiplier.getF();
            case APOTHEOTIC -> config.apotheoticAffixTemplateCostMultiplier.getF();
        };
    }

    public record EntryCost(Operation operation, AffixTemplateEntry entry, float fromLevel, float resultLevel) {}

    public enum Operation {
        EXTRACT_SNAPSHOT,
        APPLY_NEW_TEMPLATE,
        APPLY_UPGRADE_DELTA,
        MERGE_UPGRADE_DELTA;

        public float multiplier() {
            var config = CEIAXConfig.server().affixes();
            return switch (this) {
                case EXTRACT_SNAPSHOT -> config.blazeComposerExtractSnapshotMultiplier.getF();
                case APPLY_NEW_TEMPLATE -> config.blazeComposerApplyNewTemplateMultiplier.getF();
                case APPLY_UPGRADE_DELTA -> config.blazeComposerApplyUpgradeDeltaMultiplier.getF();
                case MERGE_UPGRADE_DELTA -> config.blazeComposerMergeUpgradeDeltaMultiplier.getF();
            };
        }
    }
}
