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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.data;

import com.google.gson.JsonArray;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.data.CEIConditionalLootTables;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks;

public class CEIAConditionalLootTableProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CEIAConditionalLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(provider -> CompletableFuture.allOf(
                selfDrop(provider, CEIABlocks.INFUSER.getId()),
                selfDrop(provider, CEIABlocks.BRASS_BOOKSHELF.getId()),
                selfDrop(provider, CEIABlocks.CREATIVE_BOOKSHELF.getId()),
                enderWovenBag(provider)));
    }

    private CompletableFuture<?> selfDrop(HolderLookup.Provider provider, ResourceLocation block) {
        return CEIConditionalLootTables.saveBlock(
                output,
                block,
                CEIConditionalLootTables.selfDroppingBlock(block, provider, ModIntegration.APOTHIC_ENCHANTING.condition()));
    }

    private CompletableFuture<?> enderWovenBag(HolderLookup.Provider provider) {
        var block = CEIABlocks.ENDER_WOVEN_BAG.getId();
        var entry = CEIConditionalLootTables.itemEntry(block);
        var functions = new JsonArray();
        functions.add(CEIConditionalLootTables.copyNbt(
                "Entities", CEIItemData.ROOT_TAG + "." + CEIItemData.STORED_ENTITIES_TAG));
        entry.add("functions", functions);
        return CEIConditionalLootTables.saveBlock(
                output,
                block,
                CEIConditionalLootTables.block(block, entry, provider, ModIntegration.APOTHIC_ENCHANTING.condition()));
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Apothic Enchanting Conditional Loot Tables";
    }
}
