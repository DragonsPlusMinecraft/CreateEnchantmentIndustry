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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class CEIAServerConfig extends ConfigBase {
    private final CEIAStatsConfig stats = nested(0, CEIAStatsConfig::new, Comments.bookshelf);
    private final CEIAFluidConfig fluids = nested(0, CEIAFluidConfig::new, Comments.fluids);
    private final CEIAStressConfig stress = nested(0, CEIAStressConfig::new, Comments.stress);
    private final CEIAUtilityConfig utility = nested(0, CEIAUtilityConfig::new, Comments.utility);

    @Override
    public void registerAll(ForgeConfigSpec.Builder builder) {
        super.registerAll(builder);
    }

    @Override
    public String getName() {
        return "server";
    }

    public CEIAStatsConfig stats() {
        return stats;
    }

    public CEIAFluidConfig fluids() {
        return fluids;
    }

    public CEIAStressConfig stress() {
        return stress;
    }

    public CEIAUtilityConfig utility() {
        return utility;
    }

    static class Comments {
        static final String bookshelf = "Parameters and abilities about Stats";
        static final String fluids = "Parameters and abilities of fluids and fluid operating components";
        static final String stress = "Fine tune the kinetic stats of individual components";
        static final String utility = "Parameters and abilities of various utility components";
    }
}
