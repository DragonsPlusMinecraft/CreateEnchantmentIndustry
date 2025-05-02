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

public class CEIPrinterConfig extends ConfigBase {
    public final ConfigFloat enchantedBookCostMultiplier = f(1, 0, 10, "enchantedBookCostMultiplier",
            Comments.enchantedBookCostMultiplier);
    public final ConfigBool treasureEnchantsPrintable = b(true, "treasureEnchantsPrintable",
            Comments.treasureEnchantsPrintable);

    @Override
    public String getName() {
        return "printer";
    }

    static class Comments {
        static final String enchantedBookCostMultiplier = "Cost multiplier for enchanting books in Printer. 1.0 means no cost increase, 2.0 means double cost, etc.";
        static final String treasureEnchantsPrintable = "Whether books with treasure enchantments are printable in Printer.";
    }
}
