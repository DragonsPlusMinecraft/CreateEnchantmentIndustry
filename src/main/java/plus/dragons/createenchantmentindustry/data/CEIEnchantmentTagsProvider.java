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

package plus.dragons.createenchantmentindustry.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.data.ExistingFileHelper;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;

/** Backports the semantic enchantment tags introduced after 1.20.1. */
public class CEIEnchantmentTagsProvider extends TagsProvider<Enchantment> {
    public CEIEnchantmentTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, Registries.ENCHANTMENT, lookupProvider, CEICommon.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var enchanting = tag(CEIEnchantments.MOD_TAGS.enchanting);
        tag(CEIEnchantments.MOD_TAGS.enchantingExclusive);
        var superEnchantingExclusive = tag(CEIEnchantments.MOD_TAGS.superEnchantingExclusive);
        var penaltyCurses = tag(CEIEnchantments.MOD_TAGS.penaltyCurses);
        tag(CEIEnchantments.MOD_TAGS.penaltyCursesDeny);
        tag(CEIEnchantments.MOD_TAGS.printingDeny);

        provider.lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(holder -> {
            Enchantment enchantment = holder.value();
            if (enchantment.isDiscoverable() && !enchantment.isTreasureOnly())
                addOptional(enchanting, holder);
            if (enchantment.isTreasureOnly() && !enchantment.isCurse())
                addOptional(superEnchantingExclusive, holder);
            if (enchantment.isCurse())
                addOptional(penaltyCurses, holder);
        });

        tag(CEIEnchantments.MOD_TAGS.superEnchanting)
                .addTag(CEIEnchantments.MOD_TAGS.enchanting)
                .addTag(CEIEnchantments.MOD_TAGS.superEnchantingExclusive)
                .remove(CEIEnchantments.MOD_TAGS.enchantingExclusive);
    }

    private static void addOptional(TagAppender<Enchantment> appender, Holder.Reference<Enchantment> holder) {
        appender.addOptional(holder.key().location());
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Enchantment Tags";
    }
}
