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
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
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
        scene.idle(5);

        scene.overlay().showText(60)
                .text("This is a Blaze Enchanter, which functions like an Enchanting Table")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(3, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 24000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 9000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 17000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(25);

        scene.overlay().showText(60)
                .text("Pass in Liquid Experience to get it started")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().everywhere(), 128);
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.getNormalTank().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(50);

        var slotVec = util.vector().of(2, 2.5, 1.5);
        scene.overlay().showFilterSlotInput(slotVec, Direction.WEST, 70);
        scene.overlay().showText(70)
                .text("Before you can start enchanting with it, you need to set the enchanting level via the panel. The level cap of a vanilla enchanting table is 30")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(slotVec);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setValue(30);
                });
        scene.idle(75);

        scene.addKeyframe();
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(Items.DIAMOND_SWORD.getDefaultInstance(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 2, 1), Pointing.RIGHT, 20).withItem(Items.DIAMOND_SWORD.getDefaultInstance());
        scene.idle(100);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));

        scene.overlay().showText(80)
                .text("The panel is also a filter slot. Right click that slot with an item to set the item as a template item. This is Template Enchanting Mode")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.DIAMOND_SWORD.getDefaultInstance());
                });
        scene.overlay().showControls(slotVec, Pointing.UP, 75).rightClick().withItem(Items.DIAMOND_SWORD.getDefaultInstance());
        scene.idle(85);

        scene.overlay().showText(80)
                .text("In Template Enchanting Mode, Blaze Enchanter can enchant Enchanting Template and will only generate enchantments that can appear on template item")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 2, 1), Pointing.RIGHT, 20).withItem(CEIItems.ENCHANTING_TEMPLATE.asStack());
        scene.idle(100);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));

        scene.overlay().showText(55)
                .text("Template Enchanting Mode is used to produce the specified type of enchantment")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(60);
        scene.overlay().showText(65)
                .text("For example, if you want the chest armor enchantment, you can fill the filter slot with diamond chest armor")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.DIAMOND_CHESTPLATE.getDefaultInstance());
                });
        scene.overlay().showControls(slotVec, Pointing.UP, 50).rightClick().withItem(Items.DIAMOND_CHESTPLATE.getDefaultInstance());
        scene.idle(70);
        scene.overlay().showText(80)
                .text("Then give a blank Enchanting Template to Blaze and it will spawn enchantments that can be used on chest armor on that Enchanting Template")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.idle(100);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.extractItem(true, false));

        scene.overlay().showText(65)
                .text("The same way you get chest armor enchantments, you can put in a different kind of chest armor, such as a golden chest armor, which will produce different result")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.GOLDEN_CHESTPLATE.getDefaultInstance());
                });
        scene.overlay().showControls(slotVec, Pointing.UP, 50).rightClick().withItem(Items.GOLDEN_CHESTPLATE.getDefaultInstance());
        scene.idle(70);
        scene.overlay().showText(80)
                .text("Because golden chest armor has better enchantability compared to diamond chest armor, it produces relatively better enchantments")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.idle(100);

        scene.overlay().showText(80)
                .text("Please note! Higher Enchanting Level is not always better, some enchantments will not appear when Enchanting Level is too high!")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(100);
    }

    public static void superEnchant(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_enchanter.super_enchant", "Super Enchant Time!");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(60)
                .colored(PonderPalette.BLUE)
                .text("You may have noticed that the Blaze Enchanter has two \"stomachs\". Try feeding him some Cake o' Enchanting?")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));

        scene.idle(20);
        scene.overlay().showControls(util.vector().centerOf(2, 2, 1), Pointing.RIGHT, 20).rightClick().withItem(CEIItems.EXPERIENCE_CAKE.asStack());
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000)));
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.idle(15);

        scene.overlay().showText(40)
                .text("Blaze Enchanter is in Seething mode, or we call it Super Enchanting mode")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(45);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .text("Blaze Enchanter in Super Enchanting mode has received a huge boost to its adjustable Enchanting Level cap, and it can now produce Treasure Enchants")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> {
                    var enchanter = be.getBehaviour(EnchanterBehaviour.TYPE);
                    enchanter.setTemplate(Items.DIAMOND_SWORD.getDefaultInstance());
                    enchanter.setValue(60);
                });
        scene.idle(105);

        scene.overlay().showText(55)
                .attachKeyFrame()
                .text("Blaze Enchanter in Super Enchanting mode only accepts Super Enchanting Template")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(60);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(3, 2, 1), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.idle(10);
        scene.world().setBlock(util.grid().at(1, 2, 1), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.overlay().showText(40)
                .text("Trust me, you're gonna need this")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 2, 1));
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeEnchanterBlockEntity.class,
                be -> be.insertItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack(), false));
        scene.overlay().showControls(util.vector().centerOf(2, 2, 1), Pointing.RIGHT, 20).withItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack());
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
        scene.idle(45);

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
        scene.idle(25);

        scene.overlay().showText(80)
                .text("Please be careful! If you do nothing to protect it, a lightning strike will destroy everything")
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
        scene.overlay().showControls(util.vector().centerOf(2, 2, 1), Pointing.RIGHT, 20).withItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack());
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
}
