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
import net.minecraftforge.common.ForgeConfigSpec;

public class CEIAXServerConfig extends ConfigBase {
    private final CEIAXFluidConfig fluids = nested(0, CEIAXFluidConfig::new, Comments.fluids);
    private final CEIAXUtilityConfig utility = nested(0, CEIAXUtilityConfig::new, Comments.utility);
    private final CEIAXAffixConfig affixes = nested(0, CEIAXAffixConfig::new, Comments.affixes);

    @Override
    public void registerAll(ForgeConfigSpec.Builder builder) {
        super.registerAll(builder);
    }

    @Override
    public String getName() {
        return "ex-server";
    }

    public CEIAXFluidConfig fluids() {
        return fluids;
    }

    public CEIAXUtilityConfig utility() {
        return utility;
    }

    public CEIAXAffixConfig affixes() {
        return affixes;
    }

    static class Comments {
        static final String fluids = "Parameters and abilities of fluids and fluid operating components";
        static final String utility = "Parameters and abilities of various utility components";
        static final String affixes = "Parameters and rules of Apotheosis affix composing";
    }
}
