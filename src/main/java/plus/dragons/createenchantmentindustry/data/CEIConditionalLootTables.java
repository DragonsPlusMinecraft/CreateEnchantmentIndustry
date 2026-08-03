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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public final class CEIConditionalLootTables {
    private CEIConditionalLootTables() {}

    public static JsonObject selfDroppingBlock(ResourceLocation block, HolderLookup.Provider registries, ICondition condition) {
        return block(block, itemEntry(block), registries, condition);
    }

    public static JsonObject block(ResourceLocation block, JsonObject entry, HolderLookup.Provider registries, ICondition condition) {
        var table = new JsonObject();
        table.addProperty("type", "minecraft:block");
        table.add("pools", pools(entry));
        // The block remains registered when its feature is disabled, so its recovery loot table must
        // remain available as well. Forge 1.20.1 has no general conditional loot-table wrapper.
        return table;
    }

    public static JsonObject itemEntry(ResourceLocation item) {
        var entry = new JsonObject();
        entry.addProperty("type", "minecraft:tag");
        entry.addProperty("name", optionalDropTag(item).toString());
        entry.addProperty("expand", true);
        return entry;
    }

    public static JsonObject copyNbt(String sourcePath, String targetPath) {
        var function = new JsonObject();
        function.addProperty("function", "minecraft:copy_nbt");
        function.addProperty("source", "block_entity");
        var operation = new JsonObject();
        operation.addProperty("source", sourcePath);
        operation.addProperty("target", targetPath);
        operation.addProperty("op", "replace");
        var operations = new JsonArray();
        operations.add(operation);
        function.add("ops", operations);
        return function;
    }

    public static CompletableFuture<?> saveBlock(PackOutput output, ResourceLocation block, JsonObject table) {
        var pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
        // These tables intentionally overwrite Registrate's default block loot tables at
        // the same path. The shared hash cache can otherwise skip the second write and
        // leave the unconditional table on disk.
        return DataProvider.saveStable(CachedOutput.NO_CACHE, table, pathProvider.json(block.withPrefix("blocks/")));
    }

    public static ResourceLocation optionalDropTag(ResourceLocation item) {
        return CEICommon.asResource("optional_block_drops/" + item.getPath());
    }

    private static JsonArray pools(JsonObject entry) {
        var pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0.0);
        pool.add("conditions", explosionConditions());
        var entries = new JsonArray();
        entries.add(entry);
        pool.add("entries", entries);
        pool.addProperty("rolls", 1.0);
        var pools = new JsonArray();
        pools.add(pool);
        return pools;
    }

    private static JsonArray explosionConditions() {
        var condition = new JsonObject();
        condition.addProperty("condition", "minecraft:survives_explosion");
        var conditions = new JsonArray();
        conditions.add(condition);
        return conditions;
    }
}
