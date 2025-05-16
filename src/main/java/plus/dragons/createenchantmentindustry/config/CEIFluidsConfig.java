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

package plus.dragons.createenchantmentindustry.config;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.config.ui.ConfigAnnotations.RequiresRestart;

public class CEIFluidsConfig extends ConfigBase {
    public final ConfigBool experienceVaporizeOnPlacement = b(true,
            "experienceVaporizeOnPlacement",
            Comments.experienceVaporizeOnPlacement);
    public final ConfigInt printerFluidCapacity = i(4000, 1000,
            "printerFluidCapacity",
            Comments.printerFluidCapacity,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool enableWrittenBookPrinting = b(true,
            "enableWrittenBookPrinting",
            Comments.enableWrittenBookPrinting,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool enableEnchantedBookPrinting = b(true,
            "enableEnchantedBookPrinting",
            Comments.enableEnchantedBookPrinting,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool enableCreateCopiableItemPrinting = b(true,
            "enableCreateCopiableItemPrinting",
            Comments.enableCreateCopiableItemPrinting,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool enablePackagePatternPrinting = b(true,
            "enablePackagePatternPrinting",
            Comments.enablePackagePatternPrinting,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool enablePackageAddressPrinting = b(true,
            "enablePackageAddressPrinting",
            Comments.enablePackageAddressPrinting,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool enableCustomNamePrinting = b(true,
            "enableCustomNamePrinting",
            Comments.enableCustomNamePrinting,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool printingCustomNameAsItemName = b(false,
            "printingCustomNameAsItemName",
            Comments.printingCustomNameAsItemName);
    public final ConfigInt printingGenerationChange = i(-3, -3, 1,
            "printingGenerationChange",
            Comments.printingGenerationChange);
    public final ConfigFloat printingEnchantedBookCostMultiplier = f(1f, 0.01f, 100f,
            "printingEnchantedBookCostMultiplier",
            Comments.printingEnchantedBookCostMultiplier);
    public final ConfigBool printingEnchantedBookDenylistStopCopying = b(true,
            "printingEnchantedBookDenylistStopCopying",
            Comments.printingEnchantedBookDenylistStopCopying);
    public final ConfigInt blazeEnchanterFluidCapacity = i(4000, 1000,
            "blazeEnchanterFluidCapacity",
            Comments.blazeEnchanterFluidCapacity,
            RequiresRestart.SERVER.asComment());
    public final ConfigInt blazeForgerFluidCapacity = i(4000, 1000,
            "blazeForgerFluidCapacity",
            Comments.blazeForgerFluidCapacity,
            RequiresRestart.SERVER.asComment());
    public final ConfigInt experienceLanternFluidCapacity = i(1000, 100,
            "experienceLanternFluidCapacity",
            Comments.experienceLanternFluidCapacity,
            RequiresRestart.SERVER.asComment());
    public final ConfigInt experienceLanternDrainRate = i(50, 1,
            "experienceLanternDrainRate",
            Comments.experienceLanternDrainRate,
            RequiresRestart.SERVER.asComment());
    public final ConfigBool experienceLanternPullToggle = b(true,
            "experienceLanternPullToggle",
            Comments.experienceLanternPullToggle);
    public final ConfigInt experienceLanternPullRadius = i(10, 0,
            "experienceLanternPullRadius",
            Comments.experienceLanternPullRadius);
    public final ConfigFloat experienceLanternPullForceMultiplier = f(.075f, 0.0f, .5f,
            "experienceLanternPullForceMultiplier",
            Comments.experienceLanternPullForceMultiplier);

    @Override
    public String getName() {
        return "fluids";
    }

    static class Comments {
        static final String experienceVaporizeOnPlacement = "Whether Liquid Experience will vaporize into Experience Orbs upon placement.";
        static final String printerFluidCapacity = "The amount of liquid a Printer can hold (mB).";
        static final String enableWrittenBookPrinting = "If the printing of Written Books function of Printers should be enabled.";
        static final String enableEnchantedBookPrinting = "If the printing of Enchanted Books function of Printers should be enabled.";
        static final String enableCreateCopiableItemPrinting = "If the printing of Create's copiable items function of Printers should be enabled.";
        static final String enablePackagePatternPrinting = "If the changing package patterns function of Printers should be enabled.";
        static final String enablePackageAddressPrinting = "If the assigning package addresses function of Printers should be enabled.";
        static final String enableCustomNamePrinting = "If the assigning custom names function of Printers should be enabled.";
        static final String printingCustomNameAsItemName = "Whether printing custom names (displayed in italics) should instead print as as item names (displayed in non-italics).";
        static final String printingGenerationChange = "The generation change when copying Written Books; " +
                "a value of 1 will prevent copying copy of copy";
        static final String printingEnchantedBookCostMultiplier = "The cost multiplier of printing Enchanted Books.";
        static final String printingEnchantedBookDenylistStopCopying = "Whether the Printer denylist prevents Enchanted Books from being copied." +
                "Setting this to false allows copying Enchanted Books without denied enchantments";
        static final String blazeEnchanterFluidCapacity = "The amount of liquid a Blaze Enchanter can hold (mB).";
        static final String blazeForgerFluidCapacity = "The amount of liquid a Blaze Forger can hold (mB).";
        static final String experienceLanternFluidCapacity = "The amount of liquid an Experience Lantern can hold (mB).";
        static final String experienceLanternDrainRate = "The amount of experience an Experience Lantern can drain from player per 0.5 ticks (mB).";
        static final String experienceLanternPullToggle = "Whether the Experience Lantern will pull in experience orbs from nearby.";
        static final String experienceLanternPullRadius = "The range at which experience orbs will be pulled into the lantern.";
        static final String experienceLanternPullForceMultiplier = "Modifier for the amount of force with which to pull the experience orbs.";
    }
}
