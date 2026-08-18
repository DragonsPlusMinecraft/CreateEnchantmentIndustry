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

package plus.dragons.createenchantmentindustry.integration.apotheosis.client.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerMode;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.OverlimitAffixHelper;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateOps;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXItems;

public class BlazeComposerScene {
    private static final ResourceLocation DEMO_AFFIX = new ResourceLocation("apotheosis", "sword/attribute/violent");
    private static final ResourceLocation DEMO_RARITY = new ResourceLocation("apotheosis", "rare");
    private static final ResourceLocation DEMO_SUPER_ACTIVATOR = new ResourceLocation("apotheosis", "mythic_material");
    private static final int ESSENCE_BATCH = 8000;
    private static final int PROCESSING_TICKS = 65;

    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_composer.intro", "Introduction to Blaze Composer");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);

        var composerPos = util.grid().at(2, 2, 1);
        var composer = util.select().position(composerPos);
        var slotVec = util.vector().of(2, 2.5, 1.5);
        NormalDemo demo = normalDemo();

        scene.overlay().showText(70)
                .text("The Blaze Composer transfers, combines, and reapplies Apotheosis affixes")
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Supply Apotheotic Essence through the bottom to activate Normal Mode")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 1));
        scene.overlay().showOutline(PonderPalette.INPUT, composer, util.select().fromTo(1, 1, 1, 2, 1, 3), 70);
        scene.world().setKineticSpeed(util.select().everywhere(), 128);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class, be -> {
            fillSupplyTank(be, ESSENCE_BATCH);
        });
        scene.idle(20);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, BlazeComposerScene::fillNormalTank);
        scene.world().modifyBlock(composerPos,
                state -> state.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(60);

        scene.overlay().showText(70)
                .text("Normal Mode accepts Brass and Crystal Affix Templates; each tier has its own level capacity")
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 40)
                .withItem(CEIAXItems.BRASS_AFFIX_TEMPLATE.asStack());
        scene.idle(80);

        scene.overlay().showFilterSlotInput(slotVec, Direction.WEST, 180);
        scene.overlay().showText(55)
                .text("Use the side panel to select Extract Mode...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(slotVec);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.setMode(BlazeComposerMode.EXTRACT));
        scene.idle(60);
        scene.overlay().showText(55)
                .text("...Merge Mode...")
                .placeNearTarget()
                .pointAt(slotVec);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.setMode(BlazeComposerMode.MERGE));
        scene.idle(60);
        scene.overlay().showText(55)
                .text("...or Apply Mode")
                .placeNearTarget()
                .pointAt(slotVec);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.setMode(BlazeComposerMode.APPLY));
        scene.idle(65);

        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 60)
                .withItem(AllItems.GOGGLES.asStack());
        scene.overlay().showText(80)
                .text("Engineer's Goggles preview the required inputs, essence cost, result, and any reason an operation cannot start")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(90);

        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            fillNormalTank(be);
            be.setMode(BlazeComposerMode.EXTRACT);
            be.insertItem(demo.affixedEquipment().copy(), false);
        });
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 30)
                .withItem(demo.affixedEquipment());
        scene.idle(35);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.insertItem(demo.blankTemplate().copy(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 30)
                .withItem(demo.blankTemplate());
        scene.overlay().showText(90)
                .text("Extract Mode moves one affix from equipment to a matching blank template and returns the equipment without that affix")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(PROCESSING_TICKS);
        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 35)
                .withItem(demo.filledTemplate());
        scene.idle(40);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            be.extractItem(false);
            be.extractItem(false);
        });
        scene.idle(15);

        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            fillNormalTank(be);
            be.setMode(BlazeComposerMode.MERGE);
            be.insertItem(demo.filledTemplate().copy(), false);
        });
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 30)
                .withItem(demo.filledTemplate());
        scene.idle(35);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.insertItem(demo.filledTemplate().copy(), false));
        scene.overlay().showText(95)
                .text("Merge Mode combines templates of the same tier and rarity; two matching level 0.25 affixes produce level 0.5")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(PROCESSING_TICKS);
        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 35)
                .withItem(demo.mergedTemplate());
        scene.idle(40);
        scene.overlay().showText(70)
                .text("Different affixes can also be collected together on the resulting template")
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(75);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(15);

        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            fillNormalTank(be);
            be.setMode(BlazeComposerMode.APPLY);
            be.insertItem(demo.cleanEquipment().copy(), false);
        });
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 30)
                .withItem(demo.cleanEquipment());
        scene.idle(35);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.insertItem(demo.mergedTemplate().copy(), false));
        scene.overlay().showText(95)
                .text("Apply Mode consumes the template and adds or upgrades every compatible affix on the target equipment")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(PROCESSING_TICKS);
        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 35)
                .withItem(demo.appliedEquipment());
        scene.idle(40);

        scene.overlay().showText(75)
                .colored(PonderPalette.RED)
                .text("Every output must be removed before the Composer will accept the next batch")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.idle(85);
    }

    public static void superComposing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_composer.super_composing", "Super Composing with Blaze Composer");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);

        var composerPos = util.grid().at(2, 2, 1);
        var rodPos = util.grid().at(2, 2, 2);
        var supplyTankPos = util.grid().at(1, 1, 3);
        var supplyPumpPos = util.grid().at(1, 1, 2);
        var supplyLine = util.select().fromTo(1, 1, 1, 2, 1, 3);
        SuperDemo demo = superDemo();
        ItemStack activator = superActivator();

        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 128);
        scene.world().setKineticSpeed(util.select().position(3, 2, 3), -128);
        scene.world().modifyBlockEntity(supplyTankPos, FluidTankBlockEntity.class,
                be -> fillSupplyTank(be, ESSENCE_BATCH));
        scene.idle(15);
        scene.world().modifyBlockEntity(supplyTankPos, FluidTankBlockEntity.class,
                be -> fillSupplyTank(be, ESSENCE_BATCH));
        scene.idle(15);

        scene.overlay().showText(100)
                .colored(PonderPalette.BLUE)
                .text("Fill the Normal tank through the bottom pipe, then give the Composer a Mythic Material to permanently unlock its second tank")
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.overlay().showOutline(PonderPalette.INPUT, supplyLine, supplyLine, 100);
        scene.world().setKineticSpeed(util.select().position(supplyPumpPos), 128);
        scene.world().propagatePipeChange(supplyPumpPos);
        transferEssence(scene, supplyTankPos, composerPos, ESSENCE_BATCH);
        scene.world().modifyBlock(composerPos,
                state -> state.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(25);
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 45)
                .rightClick()
                .withItem(activator);
        scene.idle(35);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.insertItem(activator.copy(), false));
        scene.idle(30);

        scene.overlay().showText(90)
                .text("Keep the bottom pipe supplied after the unlock; once the Normal tank is full, more Apotheotic Essence flows into the second tank and activates Super Mode")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(supplyPumpPos));
        scene.overlay().showOutline(PonderPalette.INPUT, supplyLine, supplyLine, 90);
        scene.idle(15);
        transferEssence(scene, supplyTankPos, composerPos, ESSENCE_BATCH);
        scene.world().modifyBlock(composerPos,
                state -> state.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.idle(80);

        scene.overlay().showText(90)
                .text("Super Mode accepts only Apotheotic Affix Templates, which can carry levels beyond an affix's native limit")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 45)
                .withItem(demo.template());
        scene.idle(100);
        scene.overlay().showText(85)
                .text("Actual level limits and essence costs follow server configuration; check them with Engineer's Goggles")
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 45)
                .withItem(AllItems.GOGGLES.asStack());
        scene.idle(95);

        scene.world().setBlock(rodPos, Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.overlay().showText(85)
                .text("With a clear path to the sky, an exposed nearby Lightning Rod safely redirects the strike with no penalty")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(rodPos));
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            be.setMode(BlazeComposerMode.APPLY);
            be.insertItem(demo.cleanEquipment().copy(), false);
            be.insertItem(demo.template().copy(), false);
        });
        scene.idle(45);
        createLightning(scene, rodPos);
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 30)
                .withItem(demo.appliedEquipment());
        scene.idle(40);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(15);

        scene.scaleSceneView(.8f);
        scene.world().setBlock(util.grid().at(2, 4, 1), Blocks.OBSIDIAN.defaultBlockState(), false);
        scene.idle(8);
        scene.world().setBlock(util.grid().at(2, 5, 1), Blocks.OBSIDIAN.defaultBlockState(), false);
        scene.idle(8);
        scene.world().setBlock(util.grid().at(2, 6, 1), Blocks.OBSIDIAN.defaultBlockState(), false);
        scene.overlay().showText(100)
                .colored(PonderPalette.RED)
                .text("Blocking the strike path prevents lightning, but the result randomly loses affix levels; Goggles show the possible loss range")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            be.insertItem(demo.cleanEquipment().copy(), false);
            be.insertItem(demo.template().copy(), false);
        });
        scene.idle(PROCESSING_TICKS);
        scene.overlay().showControls(util.vector().topOf(composerPos), Pointing.DOWN, 30)
                .withItem(demo.appliedEquipment());
        scene.idle(45);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(15);

        scene.world().setBlock(util.grid().at(2, 6, 1), Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(2, 5, 1), Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(2, 4, 1), Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(rodPos, Blocks.AIR.defaultBlockState(), true);
        scene.overlay().showText(85)
                .colored(PonderPalette.RED)
                .text("With a clear sky but no Lightning Rod, the strike destroys the Composer before it can finish")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(composerPos));
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            be.insertItem(demo.cleanEquipment().copy(), false);
            be.insertItem(demo.template().copy(), false);
        });
        scene.idle(45);
        createLightning(scene, composerPos);
        scene.world().setBlock(composerPos, AllBlocks.LIT_BLAZE_BURNER.getDefaultState(), false);
        scene.idle(45);
    }

    public static void automate(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_composer.automate", "Automating with Mechanical Arms");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        var composerPos = util.grid().at(2, 1, 2);
        var composer = util.select().position(composerPos);
        var equipmentDepotPos = util.grid().at(4, 1, 1);
        var equipmentDepot = util.select().position(equipmentDepotPos);
        var templateDepotPos = util.grid().at(2, 1, 4);
        var templateDepot = util.select().position(templateDepotPos);
        var inputArmPos = util.grid().at(4, 1, 3);
        var inputArm = util.select().position(inputArmPos);
        var outputDepotPos = util.grid().at(0, 1, 1);
        var outputDepot = util.select().position(outputDepotPos);
        var outputArmPos = util.grid().at(0, 1, 3);
        var outputArm = util.select().position(outputArmPos);
        NormalDemo demo = normalDemo();

        scene.world().showSection(composer, Direction.DOWN);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, be -> {
            fillNormalTank(be);
            be.setMode(BlazeComposerMode.EXTRACT);
        });
        scene.world().modifyBlock(composerPos,
                state -> state.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(10);

        scene.world().modifyBlockEntity(equipmentDepotPos, DepotBlockEntity.class,
                depot -> depot.setHeldItem(demo.affixedEquipment().copy()));
        scene.world().modifyBlockEntity(templateDepotPos, DepotBlockEntity.class,
                depot -> depot.setHeldItem(demo.blankTemplate().copy()));
        scene.world().showSection(inputArm.add(equipmentDepot).add(templateDepot), Direction.DOWN);
        scene.world().setKineticSpeed(inputArm, 64);
        scene.overlay().showText(75)
                .text("In Extract Mode, two input Depots provide affixed equipment and a blank template")
                .pointAt(util.vector().centerOf(composerPos));
        scene.overlay().showOutline(PonderPalette.INPUT, equipmentDepot, equipmentDepot.add(templateDepot), 70);
        scene.overlay().showOutline(PonderPalette.OUTPUT, composer, composer, 70);
        scene.idle(85);

        scene.overlay().showText(80)
                .text("The input Arm recognizes each item's role for the selected mode and inserts it into the correct slot")
                .attachKeyFrame();
        instructInputArm(scene, inputArmPos, equipmentDepotPos, composerPos, demo.affixedEquipment(), 0);
        instructInputArm(scene, inputArmPos, templateDepotPos, composerPos, demo.blankTemplate(), 1);
        scene.idle(PROCESSING_TICKS);

        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("Until both outputs are removed, neither the Arm nor a player can begin another batch")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(composerPos));
        scene.idle(80);

        scene.world().showSection(outputArm.add(outputDepot), Direction.DOWN);
        scene.world().setKineticSpeed(outputArm, 64);
        scene.overlay().showText(80)
                .text("The output Arm extracts the stripped equipment and the filled template one after the other")
                .attachKeyFrame();
        scene.overlay().showOutline(PonderPalette.INPUT, composer, composer, 70);
        scene.overlay().showOutline(PonderPalette.OUTPUT, outputDepot, outputDepot, 70);
        scene.idle(25);

        instructOutputArm(scene, outputArmPos, composerPos, outputDepotPos, 0, demo.strippedEquipment());
        scene.idle(35);
        scene.world().modifyBlockEntity(outputDepotPos, DepotBlockEntity.class,
                depot -> depot.setHeldItem(ItemStack.EMPTY));
        instructOutputArm(scene, outputArmPos, composerPos, outputDepotPos, 1, demo.filledTemplate());
        scene.idle(40);

        scene.overlay().showText(95)
                .text("Arms can also insert Apotheotic Essence containers and the Super activator; use a bottom pipe for continuous fluid supply")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(composerPos));
        scene.idle(105);
    }

    private static void instructInputArm(
            CreateSceneBuilder scene,
            net.minecraft.core.BlockPos armPos,
            net.minecraft.core.BlockPos depotPos,
            net.minecraft.core.BlockPos composerPos,
            ItemStack stack,
            int inputIndex) {
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, inputIndex);
        scene.idle(30);
        scene.world().modifyBlockEntity(depotPos, DepotBlockEntity.class,
                depot -> depot.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, stack, -1);
        scene.idle(15);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, stack, 0);
        scene.idle(30);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.insertAutomationItem(stack.copy(), false));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
    }

    private static void instructOutputArm(
            CreateSceneBuilder scene,
            net.minecraft.core.BlockPos armPos,
            net.minecraft.core.BlockPos composerPos,
            net.minecraft.core.BlockPos outputDepotPos,
            int composerResultSlot,
            ItemStack stack) {
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(30);
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class,
                be -> be.extractAutomationItem(composerResultSlot, 1, false));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, stack, -1);
        scene.idle(15);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, stack, 0);
        scene.idle(30);
        scene.world().modifyBlockEntity(outputDepotPos, DepotBlockEntity.class,
                depot -> depot.setHeldItem(stack.copy()));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(20);
    }

    private static void createLightning(CreateSceneBuilder scene, net.minecraft.core.BlockPos pos) {
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning == null)
                throw new IllegalStateException("Unable to create lightning for Blaze Composer Ponder scene");
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            return lightning;
        });
    }

    private static void fillNormalTank(BlazeComposerBlockEntity composer) {
        composer.getNormalTank().setFluid(new FluidStack(
                CEIAXFluids.APOTHEOTIC_ESSENCE.get(), composer.getNormalTank().getCapacity()));
    }

    private static void fillSupplyTank(FluidTankBlockEntity tank, int amount) {
        var controller = tank.getControllerBE();
        if (controller != null)
            controller.getTankInventory().fill(
                    new FluidStack(CEIAXFluids.APOTHEOTIC_ESSENCE.get(), amount),
                    IFluidHandler.FluidAction.EXECUTE);
    }

    private static void transferEssence(
            CreateSceneBuilder scene,
            net.minecraft.core.BlockPos supplyTankPos,
            net.minecraft.core.BlockPos composerPos,
            int amount) {
        scene.world().modifyBlockEntity(supplyTankPos, FluidTankBlockEntity.class, tank -> {
            var controller = tank.getControllerBE();
            if (controller != null)
                controller.getTankInventory().drain(amount, IFluidHandler.FluidAction.EXECUTE);
        });
        scene.world().modifyBlockEntity(composerPos, BlazeComposerBlockEntity.class, composer -> {
            var handler = composer.getFluidHandler(Direction.DOWN);
            if (handler != null)
                handler.fill(
                        new FluidStack(CEIAXFluids.APOTHEOTIC_ESSENCE.get(), amount),
                        IFluidHandler.FluidAction.EXECUTE);
        });
    }

    private static NormalDemo normalDemo() {
        ItemStack source = Items.DIAMOND_SWORD.getDefaultInstance();
        ItemStack blank = CEIAXItems.BRASS_AFFIX_TEMPLATE.asStack();
        ItemStack clean = Items.NETHERITE_SWORD.getDefaultInstance();
        if (DatagenModLoader.isRunningDataGen())
            return new NormalDemo(source, blank, source.copy(), blank.copy(), blank.copy(), clean, clean.copy());

        addDemoAffix(source, 0.25F);
        AffixTemplateOps.Result extracted = AffixTemplateOps.extract(false, 0, source, blank);
        if (!extracted.valid())
            return new NormalDemo(
                    source, blank, Items.DIAMOND_SWORD.getDefaultInstance(), blank.copy(), blank.copy(), clean, clean.copy());
        AffixTemplateOps.Result merged = AffixTemplateOps.merge(
                false, 0, extracted.secondaryOutput(), extracted.secondaryOutput().copy());
        if (!merged.valid())
            return new NormalDemo(
                    source,
                    blank,
                    extracted.primaryOutput(),
                    extracted.secondaryOutput(),
                    extracted.secondaryOutput(),
                    clean,
                    clean.copy());
        AffixTemplateOps.Result applied = AffixTemplateOps.apply(false, 0, clean, merged.primaryOutput());
        ItemStack appliedEquipment = applied.valid() ? applied.primaryOutput() : clean;
        return new NormalDemo(
                source,
                blank,
                extracted.primaryOutput(),
                extracted.secondaryOutput(),
                merged.primaryOutput(),
                clean,
                appliedEquipment);
    }

    private static SuperDemo superDemo() {
        ItemStack source = Items.DIAMOND_SWORD.getDefaultInstance();
        ItemStack blank = CEIAXItems.APOTHEOTIC_AFFIX_TEMPLATE.asStack();
        ItemStack clean = Items.NETHERITE_SWORD.getDefaultInstance();
        if (DatagenModLoader.isRunningDataGen())
            return new SuperDemo(blank, clean, clean.copy());

        addDemoAffix(source, 0.05F);
        AffixTemplateOps.Result extracted = AffixTemplateOps.extract(true, 0, source, blank);
        if (!extracted.valid())
            return new SuperDemo(blank, clean, clean.copy());
        AffixTemplateOps.Result applied = AffixTemplateOps.apply(true, 0, clean, extracted.secondaryOutput());
        return new SuperDemo(
                extracted.secondaryOutput(),
                clean,
                applied.valid() ? applied.primaryOutput() : clean.copy());
    }

    private static ItemStack superActivator() {
        var item = ForgeRegistries.ITEMS.getValue(DEMO_SUPER_ACTIVATOR);
        return item == null ? Items.NETHER_STAR.getDefaultInstance() : new ItemStack(item);
    }

    private static void addDemoAffix(ItemStack stack, float level) {
        var rarity = RarityRegistry.INSTANCE.holder(DEMO_RARITY);
        var affix = AffixRegistry.INSTANCE.holder(DEMO_AFFIX);
        if (!rarity.isBound() || !affix.isBound())
            return;
        AffixHelper.setRarity(stack, rarity.get());
        OverlimitAffixHelper.setAffixLevel(stack, affix, level);
    }

    private record NormalDemo(
            ItemStack affixedEquipment,
            ItemStack blankTemplate,
            ItemStack strippedEquipment,
            ItemStack filledTemplate,
            ItemStack mergedTemplate,
            ItemStack cleanEquipment,
            ItemStack appliedEquipment) {}

    private record SuperDemo(ItemStack template, ItemStack cleanEquipment, ItemStack appliedEquipment) {}
}
