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
import net.minecraftforge.common.ForgeConfigSpec;

public class CEIAXFluidConfig extends ConfigBase {
    public final ConfigInt gemCutterCostCrackedToChipped = i(100, 1,
            "gemCutterCostCrackedToChipped",
            Comments.gemCutterCostCrackedToChipped);

    public final ConfigInt gemCutterCostChippedToFlawed = i(300, 1,
            "gemCutterCostChippedToFlawed",
            Comments.gemCutterCostChippedToFlawed);

    public final ConfigInt gemCutterCostFlawedToNormal = i(800, 1,
            "gemCutterCostFlawedToNormal",
            Comments.gemCutterCostFlawedToNormal);

    public final ConfigInt gemCutterCostNormalToFlawless = i(2000, 1,
            "gemCutterCostNormalToFlawless",
            Comments.gemCutterCostNormalToFlawless);

    public final ConfigInt gemCutterCostFlawlessToPerfect = i(5000, 1,
            "gemCutterCostFlawlessToPerfect",
            Comments.gemCutterCostFlawlessToPerfect);

    public final ConfigFloat gemCutterCostMultiplier = f(1.0f, 0.0f,
            "gemCutterCostMultiplier",
            Comments.gemCutterCostMultiplier);

    public final ConfigInt affixAugmentorCostExperienceToApotheoticEssenceTotal = i(19347, 1,
            "affixAugmentorCostExperienceToApotheoticEssenceTotal",
            Comments.affixAugmentorCostExperienceToApotheoticEssenceTotal);

    public final ConfigInt affixAugmentorCostSigilToApotheoticEssenceRatio = i(81, 1,
            "affixAugmentorCostSigilToApotheoticEssenceRatio",
            Comments.affixAugmentorCostSigilToApotheoticEssenceRatio);

    @Override
    public void registerAll(ForgeConfigSpec.Builder builder) {
        super.registerAll(builder);
    }

    @Override
    public String getName() {
        return "ex-fluid";
    }

    static class Comments {
        static final String[] gemCutterCostCrackedToChipped = { "Crystal Essence cost (mB) for Gem Cutter to upgrade a Cracked gem into a Chipped gem.",
                "Gem Cutter uses explicit per-purity costs so modpack authors can tune each upgrade step directly.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] gemCutterCostChippedToFlawed = { "Crystal Essence cost (mB) for Gem Cutter to upgrade a Chipped gem into a Flawed gem.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] gemCutterCostFlawedToNormal = { "Crystal Essence cost (mB) for Gem Cutter to upgrade a Flawed gem into a Normal gem.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] gemCutterCostNormalToFlawless = { "Crystal Essence cost (mB) for Gem Cutter to upgrade a Normal gem into a Flawless gem.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] gemCutterCostFlawlessToPerfect = { "Crystal Essence cost (mB) for Gem Cutter to upgrade a Flawless gem into a Perfect gem.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] gemCutterCostMultiplier = { "Global multiplier applied to all Gem Cutter Crystal Essence costs.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };

        static final String[] affixAugmentorCostExperienceToApotheoticEssenceTotal = { "This setting affects the Apotheosis upgrade reference cost used by Affix Augmentor and Blaze Composer.",
                "In Apotheosis, Affix augmenting requires consuming 225 levels of Player Experience and 2 Sigil of Enhancement.",
                "Therefore, when calculating processing cost, to ease customization, Experience consumption is replaced by Apotheotic Essence,",
                "while Sigil of Enhancement consumption is converted into Apotheotic Essence too.",
                "This config determines the total Apotheotic Essence (mB) that replaces the default 225-level Experience Cost of Augmenting Table upgrades.",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
        static final String[] affixAugmentorCostSigilToApotheoticEssenceRatio = { "The config above has explained the calculation mechanism for the Apotheosis upgrade reference cost.",
                "This config determines the conversion ratio of Sigil of Enhancement to Apotheotic Essence (mB).",
                ConfigAnnotations.RequiresRestart.SERVER.asComment() };
    }
}
