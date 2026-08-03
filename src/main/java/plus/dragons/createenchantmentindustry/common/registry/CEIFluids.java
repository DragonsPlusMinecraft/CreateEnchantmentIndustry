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

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createdragonsplus.common.fluids.StandardDispenserBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceEffectHandler;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFluidType;
import plus.dragons.createenchantmentindustry.common.item.FoilBucketItem;

public class CEIFluids {
    private static final TagKey<Item> FORGE_BUCKETS = TagKey.create(
            Registries.ITEM, new ResourceLocation("forge", "buckets"));
    public static final FluidEntry<ForgeFlowingFluid.Source> EXPERIENCE = new FluidEntry<>(REGISTRATE,
            RegistryObject.create(REGISTRATE.asResource("experience"), ForgeRegistries.FLUIDS));
    public static final FluidEntry<ForgeFlowingFluid.Flowing> EXPERIENCE_FLOWING = REGISTRATE
            .fluid("experience", ExperienceFluidType.create())
            .lang("Liquid Experience")
            .properties(builder -> builder
                    .rarity(Rarity.UNCOMMON)
                    .lightLevel(15)
                    .fallDistanceModifier(0f)
                    .canPushEntity(false)
                    .canSwim(false)
                    .canDrown(false)
                    .pathType(BlockPathTypes.BLOCKED)
                    .adjacentPathType(BlockPathTypes.BLOCKED))
            .fluidProperties(p -> p.explosionResistance(100f))
            .tag(AllFluidTags.BOTTOMLESS_DENY.tag)
            .source(ForgeFlowingFluid.Source::new)
            .block()
            .properties(properties -> properties
                    .lightLevel((b) -> 15))
            .lang("Liquid Experience")
            .build()
            .bucket(FoilBucketItem::new)
            .lang("Bucket o' Enchanting")
            .properties(properties -> properties.rarity(Rarity.UNCOMMON))
            .tag(FORGE_BUCKETS)
            .build()
            .register();

    public static void register(IEventBus modBus) {
        modBus.register(CEIFluids.class);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> OpenPipeEffectHandler.REGISTRY.register(EXPERIENCE.get(), new ExperienceEffectHandler()));
        event.enqueueWork(CEIFluids::registerDispenserBehavior);
    }

    private static void registerDispenserBehavior() {
        DispenserBlock.registerBehavior(EXPERIENCE.getBucket().get(), StandardDispenserBehaviour.INSTANCE);
    }
}
