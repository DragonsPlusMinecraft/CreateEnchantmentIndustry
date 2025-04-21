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
import net.neoforged.neoforge.common.ModConfigSpec;

public class CEIServerConfig extends ConfigBase {
    public final CEIKineticsConfig kinetics = nested(0, CEIKineticsConfig::new, Comments.kinetics);
    public final CEIFluidsConfig fluids = nested(0, CEIFluidsConfig::new, Comments.fluids);
    public final CEIEnchantmentsConfig enchantments = nested(0, CEIEnchantmentsConfig::new, Comments.enchantments);
    public final CEIProcessingConfig processing = nested(0, CEIProcessingConfig::new, Comments.processing);

    @Override
    public void registerAll(ModConfigSpec.Builder builder) {
        super.registerAll(builder);
    }

    @Override
    public String getName() {
        return "server";
    }

    static class Comments {
        static final String kinetics = "Parameters and abilities of kinetic mechanisms";
        static String fluids = "Parameters and abilities of fluids and fluid operating components";
        static String enchantments = "Parameters and abilities of enchantment operating components";
        static String processing = "Parameters and abilities of processing mechanisms and appliances";
    }
}
