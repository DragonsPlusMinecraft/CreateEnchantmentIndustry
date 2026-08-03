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

package plus.dragons.createenchantmentindustry.integration.apotheosis.config;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.config.ui.ConfigAnnotations;

public class CEIAXAffixConfig extends ConfigBase {
    public final ConfigInt blazeComposerFluidCapacity = i(8000, 1000,
            "blazeComposerFluidCapacity",
            Comments.blazeComposerFluidCapacity);
    public final ConfigInt blazeComposerSuperFluidCapacity = i(8000, 1000,
            "blazeComposerSuperFluidCapacity",
            Comments.blazeComposerSuperFluidCapacity);
    public final ConfigInt blazeComposerProcessingTime = i(200, 1,
            "blazeComposerProcessingTime",
            Comments.blazeComposerProcessingTime);
    public final ConfigFloat blazeComposerBlockedSuperMinLevelPenalty = f(0.05f, 0.0f,
            "blazeComposerBlockedSuperMinLevelPenalty",
            Comments.blazeComposerBlockedSuperMinLevelPenalty);
    public final ConfigFloat blazeComposerBlockedSuperMaxLevelPenalty = f(0.20f, 0.0f,
            "blazeComposerBlockedSuperMaxLevelPenalty",
            Comments.blazeComposerBlockedSuperMaxLevelPenalty);

    public final ConfigFloat brassAffixTemplateMaxLevel = f(1.0f, 0.01f,
            "brassAffixTemplateMaxLevel",
            Comments.brassAffixTemplateMaxLevel);
    public final ConfigFloat crystalAffixTemplateMaxLevel = f(2.0f, 0.01f,
            "crystalAffixTemplateMaxLevel",
            Comments.crystalAffixTemplateMaxLevel);
    public final ConfigFloat apotheoticAffixTemplateMaxLevel = f(4.0f, 0.01f,
            "apotheoticAffixTemplateMaxLevel",
            Comments.apotheoticAffixTemplateMaxLevel);

    public final ConfigFloat affixTemplateMergeStep = f(0.25f, 0.01f,
            "affixTemplateMergeStep",
            Comments.affixTemplateMergeStep);

    public final ConfigInt blazeComposerExtractBaseCost = i(250, 1,
            "blazeComposerExtractBaseCost",
            Comments.blazeComposerExtractBaseCost);
    public final ConfigInt blazeComposerApplyBaseCost = i(350, 1,
            "blazeComposerApplyBaseCost",
            Comments.blazeComposerApplyBaseCost);
    public final ConfigInt blazeComposerMergeBaseCost = i(500, 1,
            "blazeComposerMergeBaseCost",
            Comments.blazeComposerMergeBaseCost);

    public final ConfigFloat blazeComposerExtractSnapshotMultiplier = f(0.08f, 0.0f,
            "blazeComposerExtractSnapshotMultiplier",
            Comments.blazeComposerExtractSnapshotMultiplier);
    public final ConfigFloat blazeComposerApplyNewTemplateMultiplier = f(0.15f, 0.0f,
            "blazeComposerApplyNewTemplateMultiplier",
            Comments.blazeComposerApplyNewTemplateMultiplier);
    public final ConfigFloat blazeComposerApplyUpgradeDeltaMultiplier = f(0.55f, 0.0f,
            "blazeComposerApplyUpgradeDeltaMultiplier",
            Comments.blazeComposerApplyUpgradeDeltaMultiplier);
    public final ConfigFloat blazeComposerMergeUpgradeDeltaMultiplier = f(0.35f, 0.0f,
            "blazeComposerMergeUpgradeDeltaMultiplier",
            Comments.blazeComposerMergeUpgradeDeltaMultiplier);
    public final ConfigFloat blazeComposerStandardOperationCostCap = f(1.0f, 0.0f,
            "blazeComposerStandardOperationCostCap",
            Comments.blazeComposerStandardOperationCostCap);
    public final ConfigFloat blazeComposerCrystalLevelMultiplier = f(1.8f, 0.01f,
            "blazeComposerCrystalLevelMultiplier",
            Comments.blazeComposerCrystalLevelMultiplier);
    public final ConfigFloat blazeComposerSuperLevelMultiplier = f(3.5f, 0.01f,
            "blazeComposerSuperLevelMultiplier",
            Comments.blazeComposerSuperLevelMultiplier);
    public final ConfigFloat blazeComposerSuperLevelExponent = f(1.65f, 1.0f,
            "blazeComposerSuperLevelExponent",
            Comments.blazeComposerSuperLevelExponent);

    public final ConfigFloat brassAffixTemplateCostMultiplier = f(1.0f, 0.01f,
            "brassAffixTemplateCostMultiplier",
            Comments.brassAffixTemplateCostMultiplier);
    public final ConfigFloat crystalAffixTemplateCostMultiplier = f(1.8f, 0.01f,
            "crystalAffixTemplateCostMultiplier",
            Comments.crystalAffixTemplateCostMultiplier);
    public final ConfigFloat apotheoticAffixTemplateCostMultiplier = f(3.0f, 0.01f,
            "apotheoticAffixTemplateCostMultiplier",
            Comments.apotheoticAffixTemplateCostMultiplier);
    public final ConfigFloat statAffixTypeCostMultiplier = f(1.0f, 0.01f,
            "statAffixTypeCostMultiplier",
            Comments.statAffixTypeCostMultiplier);
    public final ConfigFloat basicEffectAffixTypeCostMultiplier = f(1.2f, 0.01f,
            "basicEffectAffixTypeCostMultiplier",
            Comments.basicEffectAffixTypeCostMultiplier);
    public final ConfigFloat abilityAffixTypeCostMultiplier = f(1.6f, 0.01f,
            "abilityAffixTypeCostMultiplier",
            Comments.abilityAffixTypeCostMultiplier);

    public final ConfigFloat affixAugmentorCostMultiplier = f(0.55f, 0.0f,
            "affixAugmentorCostMultiplier",
            Comments.affixAugmentorCostMultiplier);
    public final ConfigFloat affixAugmentorMaxLevel = f(1.0f, 0.01f,
            "affixAugmentorMaxLevel",
            Comments.affixAugmentorMaxLevel);

    public final ConfigBool allowRarityMismatchApplying = b(false,
            "allowRarityMismatchApplying",
            Comments.allowRarityMismatchApplying);
    public final ConfigBool allowExclusiveSetBypassInSuperApplying = b(false,
            "allowExclusiveSetBypassInSuperApplying",
            Comments.allowExclusiveSetBypassInSuperApplying);
    public final ConfigBool allowExclusiveSetBypassInSuperMerging = b(false,
            "allowExclusiveSetBypassInSuperMerging",
            Comments.allowExclusiveSetBypassInSuperMerging);
    public final ConfigFloat superExclusiveSetApplyExtraCostMultiplier = f(1.0f, 0.0f,
            "superExclusiveSetApplyExtraCostMultiplier",
            Comments.superExclusiveSetApplyExtraCostMultiplier);
    public final ConfigFloat superExclusiveSetMergeExtraCostMultiplier = f(1.0f, 0.0f,
            "superExclusiveSetMergeExtraCostMultiplier",
            Comments.superExclusiveSetMergeExtraCostMultiplier);
    public final ConfigBool allowLevelIndependentAffixUpgrade = b(false,
            "allowLevelIndependentAffixUpgrade",
            Comments.allowLevelIndependentAffixUpgrade);

    @Override
    public String getName() {
        return "ex-affix";
    }

    static class Comments {
        static final String[] blazeComposerFluidCapacity = { "The amount of Apotheotic Essence (mB) the Blaze Composer can hold.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] blazeComposerSuperFluidCapacity = { "The amount of Super Apotheotic Essence (mB) the Blaze Composer can hold after its normal tank is full.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] blazeComposerProcessingTime = { "The processing time, in ticks, of one Blaze Composer operation.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String blazeComposerBlockedSuperMinLevelPenalty = "The minimum affix level loss applied to a Super Blaze Composer operation when its lightning path is blocked. The penalty is rolled when processing starts.";
        static final String blazeComposerBlockedSuperMaxLevelPenalty = "The maximum affix level loss applied to a Super Blaze Composer operation when its lightning path is blocked. The penalty is rolled when processing starts.";
        static final String brassAffixTemplateMaxLevel = "The maximum affix level a Brass Affix Template can hold. Default: 1.0, matching standard Apotheosis affixes.";
        static final String crystalAffixTemplateMaxLevel = "The maximum affix level a Crystal Affix Template can hold. Default: 2.0, matching Apotheosis' native upper range.";
        static final String apotheoticAffixTemplateMaxLevel = "The maximum affix level an Apotheotic Affix Template can hold. The implementation supports very high values; this config is the balancing cap.";
        static final String affixTemplateMergeStep = "The level gained when merging two templates with the same affix and level.";
        static final String blazeComposerExtractBaseCost = "The base Apotheotic Essence cost (mB) of extracting an affix into a blank template.";
        static final String blazeComposerApplyBaseCost = "The base Apotheotic Essence cost (mB) of applying a filled template to equipment.";
        static final String blazeComposerMergeBaseCost = "The base Apotheotic Essence cost (mB) of merging two matching affix templates.";
        static final String blazeComposerExtractSnapshotMultiplier = "Multiplier applied to the stored affix value when extracting an affix into a blank template.";
        static final String blazeComposerApplyNewTemplateMultiplier = "Multiplier applied to the stored affix value when applying a new filled template to equipment.";
        static final String blazeComposerApplyUpgradeDeltaMultiplier = "Multiplier applied to the added affix value when a filled template upgrades an existing matching affix.";
        static final String blazeComposerMergeUpgradeDeltaMultiplier = "Multiplier applied to the added affix value when merging two matching templates.";
        static final String blazeComposerStandardOperationCostCap = "Maximum non-Super level cost contribution, expressed as a multiplier of the current Apotheosis Augmenting Table upgrade reference cost. Super levels above 2.0 are not capped by this setting.";
        static final String blazeComposerCrystalLevelMultiplier = "Multiplier applied to the 1.0-2.0 level segment.";
        static final String blazeComposerSuperLevelMultiplier = "Multiplier applied to the Super level segment above 2.0.";
        static final String blazeComposerSuperLevelExponent = "Exponential growth applied to Super levels above 2.0. Use 1.0 for linear Super costs.";
        static final String brassAffixTemplateCostMultiplier = "Cost multiplier for operations using Brass Affix Templates.";
        static final String crystalAffixTemplateCostMultiplier = "Cost multiplier for operations using Crystal Affix Templates.";
        static final String apotheoticAffixTemplateCostMultiplier = "Cost multiplier for operations using Apotheotic Affix Templates.";
        static final String statAffixTypeCostMultiplier = "Cost multiplier for STAT affixes.";
        static final String basicEffectAffixTypeCostMultiplier = "Cost multiplier for POTION affixes.";
        static final String abilityAffixTypeCostMultiplier = "Cost multiplier for ABILITY affixes.";
        static final String affixAugmentorCostMultiplier = "Global multiplier for Affix Augmentor costs after Apotheosis upgrade reference, level delta, and datapack rule multipliers are applied.";
        static final String affixAugmentorMaxLevel = "The maximum affix level the Affix Augmentor can reach. Default: 1.0, matching standard Apotheosis Augmenting Table upgrades.";
        static final String allowRarityMismatchApplying = "Whether filled templates can be applied to equipment with a different existing rarity.";
        static final String allowExclusiveSetBypassInSuperApplying = "Whether Super Mode may ignore Apotheosis affix exclusive sets when applying Apotheotic templates to equipment.";
        static final String allowExclusiveSetBypassInSuperMerging = "Whether Super Mode may ignore Apotheosis affix exclusive sets when merging Apotheotic templates.";
        static final String superExclusiveSetApplyExtraCostMultiplier = "Extra Apotheotic Essence cost for each exclusive-set conflict bypassed while applying in Super Mode, expressed as a multiplier of the current Apotheosis upgrade reference cost.";
        static final String superExclusiveSetMergeExtraCostMultiplier = "Extra Apotheotic Essence cost for each exclusive-set conflict bypassed while merging in Super Mode, expressed as a multiplier of the current Apotheosis upgrade reference cost.";
        static final String allowLevelIndependentAffixUpgrade = "Whether affixes marked as level-independent by Apotheosis may be upgraded by template merging.";
    }
}
