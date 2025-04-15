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

public class CEIEnchantmentsConfig extends ConfigBase {
    public final ConfigInt blazeEnchanterMaxEnchantLevel = i(30, 0,
            "blazeEnchanterMaxEnchantLevel",
            Comments.blazeEnchanterMaxEnchantLevel);
    public final ConfigInt blazeEnchanterMaxSuperEnchantLevel = i(60, 0,
            "blazeEnchanterMaxSuperEnchantLevel",
            Comments.blazeEnchanterMaxSuperEnchantLevel);
    public final ConfigInt enchantmentMaxLevelExtension = i(1, 0, 255,
            "enchantmentMaxLevelExtension",
            Comments.enchantmentMaxLevelExtension);
    public final ConfigBool ignoreEnchantmentCompatibility = b(true,
            "ignoreEnchantmentCompatibility",
            Comments.ignoreEnchantmentCompatibility);

    @Override
    public String getName() {
        return "enchantments";
    }

    static class Comments {
        static final String blazeEnchanterMaxEnchantLevel = "The max experience level a Blaze Enchanter can use in Regular Enchanting";
        static final String blazeEnchanterMaxSuperEnchantLevel = "The max experience level a Blaze Enchanter can use in Super Enchanting";
        static final String enchantmentMaxLevelExtension = "Max enchantment level in Super Enchanting will be extended by this value";
        static final String ignoreEnchantmentCompatibility = "If Super Enchanting and Super Forging ignores enchantment compatibility";
    }
}
