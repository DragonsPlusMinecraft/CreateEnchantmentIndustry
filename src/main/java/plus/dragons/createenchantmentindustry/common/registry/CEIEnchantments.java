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

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import plus.dragons.createdragonsplus.data.tag.TagRegistry;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIEnchantments {
    public static final ModTags MOD_TAGS = new ModTags();

    public static void register(IEventBus modBus) {
        REGISTRATE.registerEnchantmentTags(MOD_TAGS);
    }

    public static class ModTags extends TagRegistry<Enchantment, RegistrateTagsProvider<Enchantment>> {
        public final TagKey<Enchantment> enchanting = tag(
                "blaze_enchanter/enchanting",
                "Blaze Enchanter Normal Enchanting Enchantments");
        public final TagKey<Enchantment> enchantingExclusive = tag(
                "blaze_enchanter/enchanting_exclusive",
                "Blaze Enchanter Normal Enchanting Exclusive Enchantments");
        public final TagKey<Enchantment> superEnchanting = tag(
                "blaze_enchanter/super_enchanting",
                "Blaze Enchanter Super Enchanting Enchantments");
        public final TagKey<Enchantment> superEnchantingExclusive = tag(
                "blaze_enchanter/super_enchanting_exclusive",
                "Blaze Enchanter Super Enchanting Exclusive Enchantments");
        public final TagKey<Enchantment> printingDeny = tag(
                "printer/deny",
                "Printer-Denied Enchantments");

        protected ModTags() {
            super(CEICommon.ID, Registries.ENCHANTMENT);
        }

        @Override
        public void generate(RegistrateTagsProvider<Enchantment> provider) {
            super.generate(provider);
            provider.addTag(enchanting)
                    .addTag(EnchantmentTags.IN_ENCHANTING_TABLE);
            provider.addTag(enchantingExclusive);
            provider.addTag(superEnchanting)
                    .addTag(enchanting)
                    .remove(enchantingExclusive)
                    .addTag(superEnchantingExclusive);
            provider.addTag(superEnchantingExclusive)
                    .addTag(EnchantmentTags.TREASURE)
                    .remove(EnchantmentTags.CURSE);
            provider.addTag(enchantingExclusive);
            provider.addTag(printingDeny);
        }
    }
}
