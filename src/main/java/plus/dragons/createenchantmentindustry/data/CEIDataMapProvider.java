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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createenchantmentindustry.common.datamap.CEIDataMapType;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFuel;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRule;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.util.CEIDyeFluids;

/** Writes the default resources consumed by the Forge 1.20.1 data-map backport. */
public class CEIDataMapProvider implements DataProvider {
    private final PackOutput output;

    public CEIDataMapProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> writes = new ArrayList<>();

        var experienceFuel = builder(CEIDataMaps.EXPERIENCE_FUEL);
        experienceFuel.add(
                CEIItems.EXPERIENCE_BUCKET.getId(),
                ExperienceFuel.normal(1000, Items.BUCKET.getDefaultInstance()));
        experienceFuel.add(CEIItems.EXPERIENCE_CAKE.getId(), ExperienceFuel.special(1000));
        experienceFuel.add(CEIItems.EXPERIENCE_CAKE_SLICE.getId(), ExperienceFuel.special(250));
        experienceFuel.add(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId(), ExperienceFuel.special(27));
        experienceFuel.add(CEIItems.SUPER_EXPERIENCE_NUGGET.getId(), ExperienceFuel.special(3));
        experienceFuel.add(AllBlocks.EXPERIENCE_BLOCK.getId(), ExperienceFuel.normal(27));
        experienceFuel.add(AllItems.EXP_NUGGET.getId(), ExperienceFuel.normal(3));
        experienceFuel.add(mod("create_sa", "heap_of_experience"), ExperienceFuel.normal(12), "create_sa");
        experienceFuel.add(mod("ars_nouveau", "experience_gem"), ExperienceFuel.normal(3), "ars_nouveau");
        experienceFuel.add(
                mod("ars_nouveau", "greater_experience_gem"), ExperienceFuel.normal(12), "ars_nouveau");
        experienceFuel.add(
                mod("mysticalagriculture", "experience_droplet"),
                ExperienceFuel.normal(10),
                "mysticalagriculture");
        writes.add(save(cachedOutput, experienceFuel));

        var fluidExperience = builder(CEIDataMaps.FLUID_UNIT_EXPERIENCE);
        fluidExperience.add(mod("cofh_core", "experience"), 25, "cofh_core");
        fluidExperience.add(mod("cyclic", "xpjuice"), 20, "cyclic");
        fluidExperience.add(mod("enderio", "xpjuice"), 20, "enderio");
        fluidExperience.add(mod("industrialforegoing", "essence"), 20, "industrialforegoing");
        fluidExperience.add(mod("mob_grinding_utils", "fluid_xp"), 20, "mob_grinding_utils");
        fluidExperience.add(mod("pneumaticcraft", "memory_essence"), 20, "pneumaticcraft");
        fluidExperience.add(mod("reliquary", "xp_juice_still"), 20, "reliquary");
        fluidExperience.add(mod("sophisticatedcore", "xp_still"), 20, "sophisticatedcore");
        fluidExperience.add(mod("justdirethings", "xp_fluid_source"), 20, "justdirethings");
        writes.add(save(cachedOutput, fluidExperience));

        TagKey<net.minecraft.world.level.material.Fluid> blackDye = CEIDyeFluids.tag(DyeColor.BLACK);
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_ADDRESS_INGREDIENT).add(blackDye, 10)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_PATTERN_INGREDIENT).add(blackDye, 100)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_COPY_INGREDIENT).add(blackDye, 10)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_CUSTOM_NAME_INGREDIENT)
                .add(id(BuiltInRegistries.FLUID, CEIFluids.EXPERIENCE.get()), 10)
                .add(CDPFluids.COMMON_TAGS.dyes, 250)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_WRITTEN_BOOK_INGREDIENT).add(blackDye, 10)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_BANNER_PATTERN_INGREDIENT)
                .add(CDPFluids.COMMON_TAGS.dyes, 100)));

        var customNameStyles = builder(CEIDataMaps.PRINTING_CUSTOM_NAME_STYLE);
        for (DyeColor color : DyeColor.values()) {
            customNameStyles.add(CEIDyeFluids.tag(color), Style.EMPTY.withColor(color.getTextColor()));
        }
        writes.add(save(cachedOutput, customNameStyles));

        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_ENCHANTED_BOOK_COST)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.FORGING_COST_MULTIPLIER)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.SPLITTING_COST_MULTIPLIER)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.SUPER_ENCHANTING_LEVEL_EXTENSION)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.ENCHANTMENT_PROCESSING_RULES)
                .add(id(BuiltInRegistries.ENCHANTMENT, Enchantments.MENDING),
                        EnchantmentProcessingRule.enchanterAndForgerExtension(0, 0))
                .add(id(BuiltInRegistries.ENCHANTMENT, Enchantments.INFINITY_ARROWS),
                        EnchantmentProcessingRule.enchanterAndForgerExtension(0, 0))));

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cachedOutput, MapBuilder<?, ?> builder) {
        ResourceLocation resource = builder.type.resource();
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(resource.getNamespace())
                .resolve(resource.getPath());
        return DataProvider.saveStable(cachedOutput, builder.root(), path);
    }

    private static <K, V> MapBuilder<K, V> builder(CEIDataMapType<K, V> type) {
        return new MapBuilder<>(type);
    }

    private static ResourceLocation mod(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    private static <T> ResourceLocation id(Registry<T> registry, T value) {
        ResourceLocation id = registry.getKey(value);
        if (id == null)
            throw new IllegalStateException("Unregistered data-map value " + value);
        return id;
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Data Maps";
    }

    private static final class MapBuilder<K, V> {
        private final CEIDataMapType<K, V> type;
        private final JsonObject values = new JsonObject();

        private MapBuilder(CEIDataMapType<K, V> type) {
            this.type = type;
        }

        private MapBuilder<K, V> add(ResourceLocation key, V value) {
            values.add(key.toString(), encode(value));
            return this;
        }

        private MapBuilder<K, V> add(TagKey<K> key, V value) {
            values.add("#" + key.location(), encode(value));
            return this;
        }

        private MapBuilder<K, V> add(ResourceLocation key, V value, String requiredMod) {
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "forge:mod_loaded");
            condition.addProperty("modid", requiredMod);
            JsonArray conditions = new JsonArray();
            conditions.add(condition);

            JsonObject wrapped = new JsonObject();
            wrapped.add("forge:conditions", conditions);
            wrapped.add("forge:value", encode(value));
            values.add(key.toString(), wrapped);
            return this;
        }

        private JsonElement encode(V value) {
            return type.codec().encodeStart(JsonOps.INSTANCE, value).getOrThrow(
                    false,
                    message -> {
                        throw new IllegalStateException("Failed to encode data map " + type.id() + ": " + message);
                    });
        }

        private JsonObject root() {
            JsonObject root = new JsonObject();
            root.add("values", values);
            return root;
        }
    }
}
