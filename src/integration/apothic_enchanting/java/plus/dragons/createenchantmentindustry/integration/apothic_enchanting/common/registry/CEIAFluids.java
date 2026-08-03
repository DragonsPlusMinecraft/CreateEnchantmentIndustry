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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry;

import static plus.dragons.createdragonsplus.common.registry.CDPFluids.COMMON_TAGS;
import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.api.event.PipeCollisionEvent;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.Ench;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createdragonsplus.common.fluids.StandardDispenserBehaviour;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonBreathFluidType;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragondBreathLiquidBlock;
import plus.dragons.createdragonsplus.common.fluids.dragonBreath.DragonsBreathOpenPipeEffect;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.data.tag.IntrinsicTagRegistry;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.crafting.CEIApotheosisModuleCondition;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

public class CEIAFluids {
    public static final ModTags MOD_TAGS = new ModTags();
    public static final FluidEntry<ForgeFlowingFluid.Source> INFUSED_DRAGON_BREATH = new FluidEntry<>(CEIACommon.REGISTRATE,
            RegistryObject.create(CEIACommon.REGISTRATE.asResource("infused_dragon_breath"), ForgeRegistries.FLUIDS));
    public static final FluidEntry<ForgeFlowingFluid.Flowing> INFUSED_DRAGON_BREATH_FLOWING = REGISTRATE
            .fluid("infused_dragon_breath",
                    CEIACommon.REGISTRATE.asResource("fluid/infused_dragon_breath_still"), // TODO texture update. Also need texture of bucket.
                    CEIACommon.REGISTRATE.asResource("fluid/infused_dragon_breath_flow"),
                    DragonBreathFluidType.create()) // TODO need redesign fluid effect
            .lang("Infused Dragon's Breath")
            .properties(properties -> properties
                    .rarity(Rarity.EPIC)
                    .density(3100)
                    .viscosity(6100)
                    .lightLevel(15)
                    .motionScale(0.07)
                    .supportsBoating(true)
                    .canSwim(false)
                    .canDrown(false)
                    .pathType(BlockPathTypes.DAMAGE_OTHER)
                    .adjacentPathType(null)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.DRAGON_FIREBALL_EXPLODE)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA))
            .fluidProperties(properties -> properties
                    .explosionResistance(200F)
                    .levelDecreasePerBlock(2)
                    .slopeFindDistance(2)
                    .tickRate(10))
            .source(ForgeFlowingFluid.Source::new)
            .block(DragondBreathLiquidBlock::new)
            .lang("Infused Dragon's Breath")
            .build()
            .bucket()
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .lang("Infused Dragon's Breath Bucket")
            .build()
            .setData(ProviderType.RECIPE, (ctx, prov) -> {
                new ProcessingRecipeBuilder<>(EmptyingRecipe::new, ctx.getId().withPath("infused_dragon_breath"))
                        .withCondition(ModIntegration.APOTHIC_ENCHANTING.condition())
                        .withCondition(CEIApotheosisModuleCondition.ENCHANTMENT)
                        .require(Ench.Items.INFUSED_BREATH.get())
                        .output(ctx.get(), 250)
                        .output(Items.GLASS_BOTTLE)
                        .build(prov);
                new ProcessingRecipeBuilder<>(FillingRecipe::new, ctx.getId().withPath("infused_dragon_breath"))
                        .withCondition(ModIntegration.APOTHIC_ENCHANTING.condition())
                        .withCondition(CEIApotheosisModuleCondition.ENCHANTMENT)
                        .require(ctx.get(), 250)
                        .require(Items.GLASS_BOTTLE)
                        .output(Ench.Items.INFUSED_BREATH.get())
                        .build(prov);
            })
            .register();

    public static class ModTags extends IntrinsicTagRegistry<Fluid, RegistrateTagsProvider.IntrinsicImpl<Fluid>> {
        public final TagKey<Fluid> infusing_ingredients = tag("infusing/ingredients", "Infusing Reagent");

        public ModTags() {
            super(CEIACommon.ID, Registries.FLUID);
        }

        @Override
        public void generate(RegistrateTagsProvider.IntrinsicImpl<Fluid> provider) {
            super.generate(provider);
            provider.addTag(COMMON_TAGS.dragonBreath)
                    .addOptional(CEICommon.asResource("infused_dragon_breath"))
                    .addOptional(CEICommon.asResource("flowing_infused_dragon_breath"));
            provider.addTag(infusing_ingredients)
                    .add(CEIFluids.EXPERIENCE.get())
                    .add(CEIFluids.EXPERIENCE_FLOWING.get());
        }
    }

    public static void register(IEventBus modBus) {
        modBus.register(CEIAFluids.class);
        REGISTRATE.registerFluidTags(MOD_TAGS);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        if (!Apotheosis.enableEnch)
            return;
        event.enqueueWork(CEIAFluids::registerFluidInteractions);
        event.enqueueWork(CEIAFluids::registerOpenPipeEffects);
        event.enqueueWork(CEIAFluids::registerDispenserBehavior);
    }

    public static void registerDispenserBehavior() {
        DispenserBlock.registerBehavior(
                INFUSED_DRAGON_BREATH.getBucket().get(), StandardDispenserBehaviour.INSTANCE);
    }

    public static class Events {
        @SubscribeEvent
        public static void onPipeCollisionFlow(final PipeCollisionEvent.Flow event) {
            FluidType first = event.getFirstFluid().getFluidType();
            FluidType second = event.getSecondFluid().getFluidType();
            if (first == ForgeMod.LAVA_TYPE.get() && second == INFUSED_DRAGON_BREATH.getType()) {
                event.setState(Blocks.END_STONE.defaultBlockState());
            } else if (second == ForgeMod.LAVA_TYPE.get() && first == INFUSED_DRAGON_BREATH.getType()) {
                event.setState(Blocks.END_STONE.defaultBlockState());
            }
        }

        @SubscribeEvent
        public static void onPipeCollisionSpill(final PipeCollisionEvent.Spill event) {
            Fluid world = event.getWorldFluid();
            Fluid pipe = event.getPipeFluid();
            FluidType worldType = world.getFluidType();
            FluidType pipeType = pipe.getFluidType();
            if (worldType == ForgeMod.LAVA_TYPE.get() && pipeType == INFUSED_DRAGON_BREATH.getType()) {
                if (world.isSource(world.defaultFluidState())) {
                    event.setState(Blocks.OBSIDIAN.defaultBlockState());
                } else {
                    event.setState(Blocks.END_STONE.defaultBlockState());
                }
            } else if (pipeType == ForgeMod.LAVA_TYPE.get() && worldType == INFUSED_DRAGON_BREATH.getType()) {
                if (pipe.isSource(pipe.defaultFluidState())) {
                    event.setState(Blocks.OBSIDIAN.defaultBlockState());
                } else {
                    event.setState(Blocks.END_STONE.defaultBlockState());
                }
            }
        }
    }

    static void registerFluidInteractions() {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                INFUSED_DRAGON_BREATH.getType(), fluidState -> fluidState.isSource() ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(CDPFluids.DRAGON_BREATH.getType(), new FluidInteractionRegistry.InteractionInformation(
                INFUSED_DRAGON_BREATH.getType(), fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState()));
        FluidInteractionRegistry.addInteraction(INFUSED_DRAGON_BREATH.getType(), new FluidInteractionRegistry.InteractionInformation(
                CDPFluids.DRAGON_BREATH.getType(), fluidState -> fluidState.isSource() ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.END_STONE.defaultBlockState()));
    }

    static void registerOpenPipeEffects() {
        OpenPipeEffectHandler.REGISTRY.register(INFUSED_DRAGON_BREATH.getSource(), new DragonsBreathOpenPipeEffect());
    }
}
