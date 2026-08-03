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

package plus.dragons.createenchantmentindustry.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIEnchantments {
    public static final ModTags MOD_TAGS = new ModTags();

    public static void register(IEventBus modBus) {}

    public static class ModTags {
        public final TagKey<Enchantment> enchanting = tag("blaze_enchanter/enchanting");
        public final TagKey<Enchantment> enchantingExclusive = tag("blaze_enchanter/enchanting_exclusive");
        public final TagKey<Enchantment> superEnchanting = tag("blaze_enchanter/super_enchanting");
        public final TagKey<Enchantment> superEnchantingExclusive = tag("blaze_enchanter/super_enchanting_exclusive");
        public final TagKey<Enchantment> penaltyCurses = tag("blaze_enchanter/penalty_curses");
        public final TagKey<Enchantment> penaltyCursesDeny = tag("blaze_enchanter/penalty_curses_deny");
        public final TagKey<Enchantment> printingDeny = tag("printer/deny");

        private static TagKey<Enchantment> tag(String path) {
            return TagKey.create(Registries.ENCHANTMENT, CEICommon.asResource(path));
        }
    }
}
