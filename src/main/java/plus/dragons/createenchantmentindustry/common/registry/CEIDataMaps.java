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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataMapProvider;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.util.Pairs;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFuel;

public class CEIDataMaps {
    public static final DataMapType<Item, ExperienceFuel> EXPERIENCE_FUEL = DataMapType
            .builder(CEICommon.asResource("experience_fuel"), Registries.ITEM, ExperienceFuel.CODEC)
            .synced(ExperienceFuel.FULL_CODEC, true)
            .build();
    public static final DataMapType<Fluid, Integer> FLUID_UNIT_EXPERIENCE = DataMapType
            .builder(CEICommon.asResource("unit/experience"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_ADDRESS_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/address/ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_PATTERN_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/pattern/ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_COPY_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/copy/ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_CUSTOM_NAME_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/custom_name/ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Style> PRINTING_CUSTOM_NAME_STYLE = DataMapType
            .builder(CEICommon.asResource("printing/custom_name/style"), Registries.FLUID, Style.Serializer.CODEC)
            .synced(Style.Serializer.CODEC, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_WRITTEN_BOOK_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/written_book/ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();

    public static void register(IEventBus modBus) {
        modBus.register(CEIDataMaps.class);
        REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, CEIDataMaps::generate);
    }

    @SubscribeEvent
    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(EXPERIENCE_FUEL);
        event.register(FLUID_UNIT_EXPERIENCE);
        event.register(PRINTING_ADDRESS_INGREDIENT);
        event.register(PRINTING_PATTERN_INGREDIENT);
        event.register(PRINTING_COPY_INGREDIENT);
        event.register(PRINTING_CUSTOM_NAME_INGREDIENT);
        event.register(PRINTING_CUSTOM_NAME_STYLE);
        event.register(PRINTING_WRITTEN_BOOK_INGREDIENT);
    }

    public static <T> Stream<Pair<Fluid, T>> getSourceFluidEntries(DataMapType<Fluid, T> type) {
        return BuiltInRegistries.FLUID.getDataMap(type)
                .entrySet()
                .stream()
                .map(Pairs.mapKey(BuiltInRegistries.FLUID::get))
                .filter(Pairs.filterFirst(fluid -> FluidHelper.convertToStill(fluid) == fluid));
    }

    public static void generate(RegistrateDataMapProvider provider) {
        provider.builder(EXPERIENCE_FUEL)
                .add(CEIItems.EXPERIENCE_BUCKET, ExperienceFuel.normal(1000, Items.BUCKET.getDefaultInstance()), false) // TODO Temporary solution for Create's bug, See https://github.com/Creators-of-Create/Create/pull/8304
                .add(CEIItems.EXPERIENCE_CAKE, ExperienceFuel.special(1000), false)
                .add(CEIItems.EXPERIENCE_CAKE_SLICE, ExperienceFuel.special(250), false)
                .add(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId(), ExperienceFuel.special(27), false)
                .add(CEIItems.SUPER_EXPERIENCE_NUGGET, ExperienceFuel.special(3), false)
                .add(AllBlocks.EXPERIENCE_BLOCK.getId(), ExperienceFuel.normal(27), false)
                .add(AllItems.EXP_NUGGET, ExperienceFuel.normal(3), false)
                .add(ResourceLocation.fromNamespaceAndPath("create_sa", "experience_heap"),
                        ExperienceFuel.normal(12), false,
                        new ModLoadedCondition("create_sa"))
                .add(ResourceLocation.fromNamespaceAndPath("ars_nouveau", "experience_gem"),
                        ExperienceFuel.normal(3), false,
                        new ModLoadedCondition("ars_nouveau"))
                .add(ResourceLocation.fromNamespaceAndPath("ars_nouveau", "greater_experience_gem"),
                        ExperienceFuel.normal(12), false,
                        new ModLoadedCondition("ars_nouveau"))
                .add(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "experience_droplet"),
                        ExperienceFuel.normal(10), false,
                        new ModLoadedCondition("mysticalagriculture"));
        provider.builder(FLUID_UNIT_EXPERIENCE)
                .add(ResourceLocation.fromNamespaceAndPath("cofh_core", "experience"),
                        25, false,
                        new ModLoadedCondition("cofh_core"))
                .add(ResourceLocation.fromNamespaceAndPath("cyclic", "xpjuice"),
                        20, false,
                        new ModLoadedCondition("cyclic"))
                .add(ResourceLocation.fromNamespaceAndPath("enderio", "xpjuice"),
                        20, false,
                        new ModLoadedCondition("enderio"))
                .add(ResourceLocation.fromNamespaceAndPath("industrialforegoing", "essence"),
                        20, false,
                        new ModLoadedCondition("industrialforegoing"))
                .add(ResourceLocation.fromNamespaceAndPath("mob_grinding_utils", "fluid_xp"),
                        20, false,
                        new ModLoadedCondition("mob_grinding_utils"))
                .add(ResourceLocation.fromNamespaceAndPath("industrialforegoing", "essence"),
                        20, false,
                        new ModLoadedCondition("industrialforegoing"))
                .add(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "memory_essence"),
                        20, false,
                        new ModLoadedCondition("pneumaticcraft"))
                .add(ResourceLocation.fromNamespaceAndPath("reliquary", "xp_juice_still"),
                        20, false,
                        new ModLoadedCondition("reliquary"))
                .add(ResourceLocation.fromNamespaceAndPath("sophisticatedcore", "xp_still"),
                        20, false,
                        new ModLoadedCondition("sophisticatedcore"));
        var blackDye = CDPFluids.COMMON_TAGS.dyesByColor.get(DyeColor.BLACK);
        provider.builder(PRINTING_ADDRESS_INGREDIENT)
                .add(blackDye, 10, false);
        provider.builder(PRINTING_PATTERN_INGREDIENT)
                .add(blackDye, 100, false);
        provider.builder(PRINTING_COPY_INGREDIENT)
                .add(blackDye, 10, false);
        provider.builder(PRINTING_CUSTOM_NAME_INGREDIENT)
                .add(CEIFluids.EXPERIENCE, 10, false)
                .add(CDPFluids.COMMON_TAGS.dyes, 250, false);
        provider.builder(PRINTING_WRITTEN_BOOK_INGREDIENT)
                .add(blackDye, 10, false);
        var customNameStyles = provider.builder(PRINTING_CUSTOM_NAME_STYLE);
        CDPFluids.COMMON_TAGS.dyesByColor.forEach((color, tag) -> customNameStyles
                .add(tag, Style.EMPTY.withColor(color.getTextColor()), false));
    }
}
