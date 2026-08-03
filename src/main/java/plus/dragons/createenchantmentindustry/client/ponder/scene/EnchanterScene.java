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

package plus.dragons.createenchantmentindustry.client.ponder.scene;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderScenes;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchanterBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public class EnchanterScene {
    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_enchanter.intro", "Introduction to Blaze Enchanter");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("This is a Blaze Enchanter, which functions like an Enchanting Table")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(3, 1, 3), FluidTankBlockEntity.class, be -> {
            var ctrl = be.getControllerBE();
            if (ctrl != null) ctrl.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 24000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class, be -> {
            var ctrl = be.getControllerBE();
            if (ctrl != null) ctrl.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 9000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 3), FluidTankBlockEntity.class, be -> {
            var ctrl = be.getControllerBE();
            if (ctrl != null) ctrl.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 17000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(30);

        scene.overlay().showText(60)
                .text("Provide it Liquid Experience to activate it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().everywhere(), 128);
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.getNormalTank().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(50);

        var slotVec = util.vector().of(2, 2.5, 1.5);
        scene.overlay().showFilterSlotInput(slotVec, Direction.WEST, 70);
        scene.overlay().showText(70)
                .text("Before enchanting with it, the enchant level must be set via the panel. The level cap of a vanilla enchanting table is 30")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(slotVec);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setValue(30);
                });
        scene.idle(80);

        scene.addKeyframe();
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(Items.DIAMOND_SWORD.getDefaultInstance(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 20).withItem(Items.DIAMOND_SWORD.getDefaultInstance());
        scene.idle(100);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));

        scene.overlay().showText(80)
                .text("The panel is also a filter slot. Right click that slot with an item to designate that item as a template. This state is called Template Enchanting Mode")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.DIAMOND_SWORD.getDefaultInstance());
                });
        scene.overlay().showControls(slotVec, Pointing.UP, 75).rightClick().withItem(Items.DIAMOND_SWORD.getDefaultInstance());
        scene.idle(90);

        scene.overlay().showText(80)
                .text("In Template Enchanting Mode, Blaze Enchanter can enchant Enchanting Template")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 20).withItem(CEIItems.ENCHANTING_TEMPLATE.asStack());
        scene.idle(100);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));

        scene.overlay().showText(60)
                .text("Template Enchanting Mode is used to produce only enchantments supported by the template item")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);
        scene.overlay().showText(80)
                .text("For example, placing diamond chest armor in the filter slot causes only diamond-chest-armor enchantments to be generated")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.DIAMOND_CHESTPLATE.getDefaultInstance());
                });
        scene.overlay().showControls(slotVec, Pointing.UP, 50).rightClick().withItem(Items.DIAMOND_CHESTPLATE.getDefaultInstance());
        scene.idle(90);
        scene.overlay().showText(80)
                .text("Give a blank Enchanting Template to Blaze Enchanter, and the aforementioned enchantments will be added to it")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.idle(100);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));

        scene.overlay().showText(65)
                .text("Different materials and template item types will produce different enchantments")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.GOLDEN_CHESTPLATE.getDefaultInstance());
                });
        scene.overlay().showControls(slotVec, Pointing.UP, 50).rightClick().withItem(Items.GOLDEN_CHESTPLATE.getDefaultInstance());
        scene.idle(75);
        scene.overlay().showText(80)
                .text("Because golden chest armor has better enchantability compared to diamond chest armor, it produces relatively better enchantments")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.idle(100);

        scene.overlay().showText(80)
                .text("However, higher Enchanting Level is not always better. Some enchantments will not appear when the Enchanting Level is too high!")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(100);
    }

    public static void superEnchant(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_enchanter.super_enchant", "Super Enchanting with Blaze Enchanter");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60)
                .colored(PonderPalette.BLUE)
                .text("The Blaze Enchanter has two \"stomachs\". Feed it a Cake o' Enchanting...")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));

        scene.idle(20);
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 20).rightClick().withItem(CEIItems.EXPERIENCE_CAKE.asStack());
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000)));
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("...and it will begin seething. This state is known as Super Enchanting mode")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("The second tank of Blaze Enchanter can not be piped in for Liquid Experience. An eligible Super Experience item, such as Cake o' Enchanting, must be used")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .text("While in Super Enchanting mode, the enchant level cap is significantly increased. Additionally, the Enchanter can now produce Treasure Enchantments")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.DIAMOND_SWORD.getDefaultInstance());
                    enchanter.setValue(60);
                });
        scene.idle(110);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("Blaze Enchanter in Super Enchanting mode exclusively processes Super Enchanting Templates")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(70);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(3, 2, 1), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.idle(10);
        scene.world().setBlock(util.grid().at(1, 2, 1), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.overlay().showText(40)
                .text("Make sure to place a Lightning Rod nearby")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 2, 1));
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 3, 1), Pointing.DOWN, 20).withItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack());
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(3, 2, 1)));
            return lightning;
        });
        scene.world().setBlock(util.grid().at(3, 1, 1), CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);

        scene.idle(20);
        scene.overlay().showText(40)
                .text("Super Enchanting can cause lightning strikes")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));
        scene.idle(50);

        scene.overlay().showText(80)
                .text("You can cover the Blaze Enchanter with block to avoid lightning strikes, but then curse may appear")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.scaleSceneView(.8f);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 4, 1), Blocks.OBSIDIAN.defaultBlockState(), false);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 5, 1), Blocks.OBSIDIAN.defaultBlockState(), false);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 6, 1), Blocks.OBSIDIAN.defaultBlockState(), false);
        scene.idle(30);

        scene.overlay().showText(80)
                .text("If no Lightning Rods are present...")
                .attachKeyFrame()
                .independent();
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 6, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 5, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 4, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().setBlock(util.grid().at(3, 2, 1), Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(util.grid().at(1, 2, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000));
                    be.insertItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack(), false);
                });
        scene.overlay().showControls(util.vector().of(2, 3, 1), Pointing.DOWN, 20).withItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack());
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(2, 2, 1)));
            return lightning;
        });
        scene.world().setBlock(util.grid().at(2, 2, 1), AllBlocks.LIT_BLAZE_BURNER.getDefaultState(), false);
        scene.world().setBlock(util.grid().at(1, 1, 1), CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);
        scene.idle(20);
    }

    public static void automate(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_enchanter.automate", "Automating with Mechanical Arm");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(10);

        var input = util.grid().at(4, 1, 3);
        var inputDepot = util.select().position(4, 1, 3);
        var armPos = util.grid().at(4, 1, 1);
        var arm = util.select().position(4, 1, 1);
        var enchanter = util.select().position(2, 1, 2);
        scene.world().modifyBlockEntity(input, DepotBlockEntity.class,
                depot -> depot.setHeldItem(Items.DIAMOND_SWORD.getDefaultInstance()));
        scene.world().showSection(arm.add(inputDepot), Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(arm, 128);
        scene.overlay().showText(60)
                .text("Blaze Enchanter can be automated with Mechanical Arm")
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.overlay().showOutline(PonderPalette.INPUT, inputDepot, inputDepot, 40);
        scene.overlay().showOutline(PonderPalette.OUTPUT, enchanter, enchanter, 40);
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Mechanical Arm can insert item for enchanting")
                .attachKeyFrame();
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(input, DepotBlockEntity.class, depot -> depot.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, Items.DIAMOND_SWORD.getDefaultInstance(), -1);
        scene.idle(20);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, Items.DIAMOND_SWORD.getDefaultInstance(), 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(Items.DIAMOND_SWORD.getDefaultInstance(), false));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, -1);
        scene.idle(50);

        scene.overlay().showText(60)
                .text("Mechanical Arm also can feed experience fuel")
                .attachKeyFrame();
        scene.world().modifyBlockEntity(input, DepotBlockEntity.class,
                depot -> depot.setHeldItem(CEIItems.EXPERIENCE_CAKE.asStack()));
        scene.idle(10);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(input, DepotBlockEntity.class, depot -> depot.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, CEIItems.EXPERIENCE_CAKE.asStack(), -1);
        scene.idle(20);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, CEIItems.EXPERIENCE_CAKE.asStack(), 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), BlazeEnchanterBlockEntity.class,
                be -> be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000)));
        scene.world().modifyBlock(util.grid().at(2, 1, 2), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, -1);
        scene.idle(20);

        var output = util.grid().at(0, 1, 3);
        var outputPos = util.select().position(0, 1, 3);
        var armPos2 = util.grid().at(0, 1, 1);
        var arm2 = util.select().position(0, 1, 1);
        scene.world().showSection(arm2.add(outputPos), Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(arm2, 128);
        scene.overlay().showText(60)
                .text("Mechanical Arm can extract enchanted item and invalid item")
                .attachKeyFrame();
        var enchanted = Items.DIAMOND_SWORD.getDefaultInstance();
        CEIPonderScenes.enchant(scene, enchanted, Enchantments.SWEEPING_EDGE, 3);
        scene.overlay().showOutline(PonderPalette.INPUT, enchanter, enchanter, 40);
        scene.overlay().showOutline(PonderPalette.OUTPUT, outputPos, outputPos, 40);
        scene.idle(40);
        scene.world().instructArm(armPos2, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(false, false));
        scene.world().instructArm(armPos2, ArmBlockEntity.Phase.SEARCH_OUTPUTS, enchanted, -1);
        scene.idle(20);
        scene.world().instructArm(armPos2, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, enchanted, 0);
        scene.idle(20);
        scene.world().instructArm(armPos2, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, -1);
        scene.world().modifyBlockEntity(output, DepotBlockEntity.class, depot -> depot.setHeldItem(enchanted));
        scene.idle(20);
    }
}
