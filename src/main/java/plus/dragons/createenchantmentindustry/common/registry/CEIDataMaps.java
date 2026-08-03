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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.foundation.fluid.FluidHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.datamap.CEIDataMapType;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFuel;
import plus.dragons.createenchantmentindustry.common.network.CEIDataMapSyncPacket;
import plus.dragons.createenchantmentindustry.common.network.CEINetwork;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRule;
import plus.dragons.createenchantmentindustry.util.CEIIntIntPair;

/** Forge 1.20.1 backport of the CEI data maps used by the 1.21 codebase. */
public final class CEIDataMaps {
    private static final Logger LOGGER = LoggerFactory.getLogger(CEIDataMaps.class);
    private static final Gson GSON = new Gson();

    public static final CEIDataMapType<Item, ExperienceFuel> EXPERIENCE_FUEL = type(
            "experience_fuel", BuiltInRegistries.ITEM, ExperienceFuel.CODEC);
    public static final CEIDataMapType<Fluid, Integer> FLUID_UNIT_EXPERIENCE = type(
            "unit/experience", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Fluid, Integer> PRINTING_ADDRESS_INGREDIENT = type(
            "printing/address/ingredient", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Fluid, Integer> PRINTING_PATTERN_INGREDIENT = type(
            "printing/pattern/ingredient", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Fluid, Integer> PRINTING_COPY_INGREDIENT = type(
            "printing/copy/ingredient", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Fluid, Integer> PRINTING_CUSTOM_NAME_INGREDIENT = type(
            "printing/custom_name/ingredient", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Fluid, Style> PRINTING_CUSTOM_NAME_STYLE = type(
            "printing/custom_name/style", BuiltInRegistries.FLUID, Style.FORMATTING_CODEC);
    public static final CEIDataMapType<Fluid, Integer> PRINTING_WRITTEN_BOOK_INGREDIENT = type(
            "printing/written_book/ingredient", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Fluid, Integer> PRINTING_BANNER_PATTERN_INGREDIENT = type(
            "printing/banner_pattern/ingredient", BuiltInRegistries.FLUID, ExtraCodecs.POSITIVE_INT);
    public static final CEIDataMapType<Enchantment, List<CEIIntIntPair>> PRINTING_ENCHANTED_BOOK_COST = type(
            "printing/enchanted_book/custom_cost", BuiltInRegistries.ENCHANTMENT, Codec.list(CEIIntIntPair.CODEC));
    public static final CEIDataMapType<Enchantment, Float> FORGING_COST_MULTIPLIER = type(
            "forging/cost_multiplier", BuiltInRegistries.ENCHANTMENT, ExtraCodecs.POSITIVE_FLOAT);
    public static final CEIDataMapType<Enchantment, Float> SPLITTING_COST_MULTIPLIER = type(
            "forging/split_enchantment_cost_multiplier", BuiltInRegistries.ENCHANTMENT, ExtraCodecs.POSITIVE_FLOAT);
    public static final CEIDataMapType<Enchantment, Integer> SUPER_ENCHANTING_LEVEL_EXTENSION = type(
            "super_enchanting/custom_level_extension", BuiltInRegistries.ENCHANTMENT, ExtraCodecs.NON_NEGATIVE_INT);
    public static final CEIDataMapType<Enchantment, EnchantmentProcessingRule> ENCHANTMENT_PROCESSING_RULES = type(
            "enchantment_processing/rules", BuiltInRegistries.ENCHANTMENT, EnchantmentProcessingRule.CODEC);

    private static final List<CEIDataMapType<?, ?>> TYPES = List.of(
            EXPERIENCE_FUEL,
            FLUID_UNIT_EXPERIENCE,
            PRINTING_ADDRESS_INGREDIENT,
            PRINTING_PATTERN_INGREDIENT,
            PRINTING_COPY_INGREDIENT,
            PRINTING_CUSTOM_NAME_INGREDIENT,
            PRINTING_CUSTOM_NAME_STYLE,
            PRINTING_WRITTEN_BOOK_INGREDIENT,
            PRINTING_BANNER_PATTERN_INGREDIENT,
            PRINTING_ENCHANTED_BOOK_COST,
            FORGING_COST_MULTIPLIER,
            SPLITTING_COST_MULTIPLIER,
            SUPER_ENCHANTING_LEVEL_EXTENSION,
            ENCHANTMENT_PROCESSING_RULES);
    private static final Map<ResourceLocation, CEIDataMapType<?, ?>> TYPES_BY_ID = indexTypes();
    private static final AtomicReference<Snapshot> SERVER = new AtomicReference<>(Snapshot.empty());
    private static final AtomicReference<Snapshot> CLIENT = new AtomicReference<>(Snapshot.empty());
    private static final AtomicReference<ResourceManager> PENDING_SERVER_RESOURCES = new AtomicReference<>();

    private CEIDataMaps() {}

    private static <K, V> CEIDataMapType<K, V> type(String path, Registry<K> registry, Codec<V> codec) {
        return new CEIDataMapType<>(CEICommon.asResource(path), registry, codec);
    }

    private static Map<ResourceLocation, CEIDataMapType<?, ?>> indexTypes() {
        Map<ResourceLocation, CEIDataMapType<?, ?>> result = new LinkedHashMap<>();
        TYPES.forEach(type -> result.put(type.id(), type));
        return Map.copyOf(result);
    }

    public static void register(IEventBus modBus) {
        CEINetwork.register();
        MinecraftForge.EVENT_BUS.addListener(CEIDataMaps::addReloadListener);
        MinecraftForge.EVENT_BUS.addListener(CEIDataMaps::tagsUpdated);
        MinecraftForge.EVENT_BUS.addListener(CEIDataMaps::sync);
    }

    private static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ReloadListener());
    }

    private static void tagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            return;
        }
        ResourceManager resources = PENDING_SERVER_RESOURCES.getAndSet(null);
        if (resources == null) {
            return;
        }
        // Forge posts TagsUpdatedEvent only after ReloadableServerResources has rebound the
        // static registry tags. Build and atomically publish the snapshot here so #tag keys
        // always resolve against the data from this exact reload.
        SERVER.set(load(resources));
    }

    private static void sync(OnDatapackSyncEvent event) {
        CEIDataMapSyncPacket packet = CEIDataMapSyncPacket.create();
        if (event.getPlayer() != null) {
            CEINetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        } else {
            CEINetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    public static void clearClientSnapshot() {
        CLIENT.set(Snapshot.empty());
    }

    public static <K, V> V get(CEIDataMapType<K, V> type, K key) {
        ResourceLocation keyId = type.registry().getKey(key);
        if (keyId == null) {
            return null;
        }
        return current().get(type, keyId);
    }

    public static <K, V> Stream<Pair<K, V>> entries(CEIDataMapType<K, V> type) {
        return current().entries(type);
    }

    public static <T> Stream<Pair<Fluid, T>> getSourceFluidEntries(CEIDataMapType<Fluid, T> type) {
        return entries(type).filter(pair -> FluidHelper.convertToStill(pair.getFirst()) == pair.getFirst());
    }

    private static Snapshot current() {
        return EffectiveSide.get() == LogicalSide.CLIENT ? CLIENT.get() : SERVER.get();
    }

    public static Map<ResourceLocation, Map<ResourceLocation, JsonElement>> serializeServerSnapshot() {
        return SERVER.get().serialize();
    }

    public static void applyClientSnapshot(Map<ResourceLocation, Map<ResourceLocation, JsonElement>> serialized) {
        try {
            CLIENT.set(decodeSnapshot(serialized));
        } catch (RuntimeException exception) {
            LOGGER.error("Rejected invalid CEI data-map snapshot from server; keeping the previous client snapshot", exception);
        }
    }

    private static Snapshot decodeSnapshot(Map<ResourceLocation, Map<ResourceLocation, JsonElement>> serialized) {
        Map<ResourceLocation, Map<ResourceLocation, Object>> result = new LinkedHashMap<>();
        serialized.forEach((typeId, entries) -> {
            CEIDataMapType<?, ?> type = TYPES_BY_ID.get(typeId);
            if (type == null) {
                throw new IllegalArgumentException("Unknown CEI data-map type " + typeId);
            }
            Map<ResourceLocation, Object> decoded = new LinkedHashMap<>();
            entries.forEach((key, value) -> decoded.put(key, decode(type, value, "network snapshot")));
            result.put(typeId, Map.copyOf(decoded));
        });
        TYPES.forEach(type -> result.putIfAbsent(type.id(), Map.of()));
        return new Snapshot(Map.copyOf(result));
    }

    private static final class ReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(
                PreparationBarrier barrier,
                ResourceManager resourceManager,
                ProfilerFiller preparationsProfiler,
                ProfilerFiller reloadProfiler,
                Executor backgroundExecutor,
                Executor gameExecutor) {
            // Forge appends mod listeners to the vanilla reload pipeline, but registry tags are
            // rebound only after that pipeline completes. Keep this reload's ResourceManager and
            // publish the snapshot from TagsUpdatedEvent instead.
            return CompletableFuture.runAsync(() -> {}, backgroundExecutor)
                    .thenCompose(barrier::wait)
                    .thenRunAsync(() -> PENDING_SERVER_RESOURCES.set(resourceManager), gameExecutor);
        }

        @Override
        public String getName() {
            return "CEI data maps";
        }
    }

    private static Snapshot load(ResourceManager resourceManager) {
        Map<ResourceLocation, Map<ResourceLocation, Object>> loaded = new LinkedHashMap<>();
        for (CEIDataMapType<?, ?> type : TYPES) {
            Map<ResourceLocation, Object> entries = new LinkedHashMap<>();
            List<Resource> resources = resourceManager.getResourceStack(type.resource());
            for (Resource resource : resources) {
                applyResource(type, entries, resource);
            }
            loaded.put(type.id(), Map.copyOf(entries));
        }
        Snapshot snapshot = new Snapshot(Map.copyOf(loaded));
        LOGGER.info("Loaded {} CEI data-map entries from {} map types", snapshot.size(), TYPES.size());
        return snapshot;
    }

    private static void applyResource(
            CEIDataMapType<?, ?> type,
            Map<ResourceLocation, Object> entries,
            Resource resource) {
        String source = type.resource() + " from pack " + resource.sourcePackId();
        JsonObject root;
        try (BufferedReader reader = resource.openAsReader()) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                throw new IllegalArgumentException(source + " must contain a JSON object");
            }
            root = parsed.getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + source, exception);
        }

        if (root.has("replace") && root.get("replace").getAsBoolean()) {
            entries.clear();
        }
        if (root.has("values")) {
            if (!root.get("values").isJsonObject()) {
                throw new IllegalArgumentException(source + " member 'values' must be an object");
            }
            JsonObject values = root.getAsJsonObject("values");
            for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                JsonElement wrapped = entry.getValue();
                if (!conditionsPass(wrapped, source + " value " + entry.getKey())) {
                    continue;
                }
                JsonElement value = unwrapValue(wrapped);
                Object decoded = decode(type, value, source + " value " + entry.getKey());
                resolve(type, entry.getKey(), true, source).forEach(key -> entries.put(key, decoded));
            }
        }
        if (root.has("remove")) {
            if (!root.get("remove").isJsonArray()) {
                throw new IllegalArgumentException(source + " member 'remove' must be an array");
            }
            JsonArray removals = root.getAsJsonArray("remove");
            for (JsonElement removal : removals) {
                if (!removal.isJsonPrimitive() || !removal.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException(source + " contains a non-string remove entry");
                }
                resolve(type, removal.getAsString(), false, source).forEach(entries::remove);
            }
        }
    }

    private static JsonElement unwrapValue(JsonElement element) {
        if (!element.isJsonObject()) {
            return element;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("neoforge:value")) {
            return object.get("neoforge:value");
        }
        if (object.has("forge:value")) {
            return object.get("forge:value");
        }
        if (object.has("value") && object.has("replace")) {
            return object.get("value");
        }
        return element;
    }

    private static boolean conditionsPass(JsonElement element, String source) {
        if (!element.isJsonObject()) {
            return true;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement conditions = firstPresent(
                object,
                "forge:conditions",
                "neoforge:conditions",
                "conditions");
        if (conditions == null) {
            return true;
        }
        if (!conditions.isJsonArray()) {
            throw new IllegalArgumentException(source + " conditions must be an array");
        }
        for (JsonElement condition : conditions.getAsJsonArray()) {
            if (!condition.isJsonObject() || !evaluateCondition(condition.getAsJsonObject(), source)) {
                return false;
            }
        }
        return true;
    }

    private static JsonElement firstPresent(JsonObject object, String... names) {
        for (String name : names) {
            if (object.has(name)) {
                return object.get(name);
            }
        }
        return null;
    }

    private static boolean evaluateCondition(JsonObject condition, String source) {
        if (!condition.has("type")) {
            throw new IllegalArgumentException(source + " condition is missing type");
        }
        String type = condition.get("type").getAsString();
        return switch (type) {
            case "forge:mod_loaded", "neoforge:mod_loaded" -> {
                if (!condition.has("modid")) {
                    throw new IllegalArgumentException(source + " mod_loaded condition is missing modid");
                }
                yield ModList.get().isLoaded(condition.get("modid").getAsString());
            }
            case "forge:not", "neoforge:not" -> {
                JsonElement child = firstPresent(condition, "value", "condition");
                if (child == null || !child.isJsonObject()) {
                    throw new IllegalArgumentException(source + " not condition requires an object value");
                }
                yield !evaluateCondition(child.getAsJsonObject(), source);
            }
            default -> throw new IllegalArgumentException(source + " uses unsupported condition " + type);
        };
    }

    private static List<ResourceLocation> resolve(
            CEIDataMapType<?, ?> type,
            String rawKey,
            boolean required,
            String source) {
        boolean tag = rawKey.startsWith("#");
        ResourceLocation id = ResourceLocation.tryParse(tag ? rawKey.substring(1) : rawKey);
        if (id == null) {
            throw new IllegalArgumentException(source + " contains invalid registry id " + rawKey);
        }
        if (tag) {
            return resolveTag(type, id);
        }
        if (!type.registry().containsKey(id)) {
            if (required) {
                throw new IllegalArgumentException(source + " references missing " + type.registry().key().location() + " " + id);
            }
            return List.of();
        }
        return List.of(id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<ResourceLocation> resolveTag(CEIDataMapType<?, ?> type, ResourceLocation id) {
        Registry registry = type.registry();
        TagKey tag = TagKey.create(registry.key(), id);
        List<ResourceLocation> values = new ArrayList<>();
        registry.getTagOrEmpty(tag).forEach(holderObject -> {
            net.minecraft.core.Holder<?> holder = (net.minecraft.core.Holder<?>) holderObject;
            ResourceLocation key = registry.getKey(holder.value());
            if (key != null) {
                values.add(key);
            }
        });
        return values;
    }

    private static Object decode(CEIDataMapType<?, ?> type, JsonElement value, String source) {
        return type.codec().parse(JsonOps.INSTANCE, value).getOrThrow(
                false,
                message -> {
                    throw new IllegalArgumentException("Failed to decode " + source + ": " + message);
                });
    }

    private record Snapshot(Map<ResourceLocation, Map<ResourceLocation, Object>> values) {
        static Snapshot empty() {
            Map<ResourceLocation, Map<ResourceLocation, Object>> empty = new LinkedHashMap<>();
            TYPES.forEach(type -> empty.put(type.id(), Map.of()));
            return new Snapshot(Map.copyOf(empty));
        }

        int size() {
            return values.values().stream().mapToInt(Map::size).sum();
        }

        @SuppressWarnings("unchecked")
        <K, V> V get(CEIDataMapType<K, V> type, ResourceLocation key) {
            return (V) values.getOrDefault(type.id(), Map.of()).get(key);
        }

        @SuppressWarnings("unchecked")
        <K, V> Stream<Pair<K, V>> entries(CEIDataMapType<K, V> type) {
            return values.getOrDefault(type.id(), Map.of()).entrySet().stream()
                    .map(entry -> Pair.of(type.registry().get(entry.getKey()), (V) entry.getValue()))
                    .filter(pair -> pair.getFirst() != null);
        }

        Map<ResourceLocation, Map<ResourceLocation, JsonElement>> serialize() {
            Map<ResourceLocation, Map<ResourceLocation, JsonElement>> result = new LinkedHashMap<>();
            for (CEIDataMapType<?, ?> type : TYPES) {
                Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
                values.getOrDefault(type.id(), Map.of()).forEach((key, value) -> entries.put(
                        key,
                        encode(type, value)));
                result.put(type.id(), Map.copyOf(entries));
            }
            return Map.copyOf(result);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static JsonElement encode(CEIDataMapType type, Object value) {
            return (JsonElement) ((Codec) type.codec()).encodeStart(JsonOps.INSTANCE, value).getOrThrow(
                    false,
                    message -> {
                        throw new IllegalStateException("Failed to encode data map " + type.id() + ": " + message);
                    });
        }
    }
}
