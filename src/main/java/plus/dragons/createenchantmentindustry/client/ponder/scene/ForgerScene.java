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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderScenes;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public class ForgerScene {
    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_forger.intro", "Introduction to Blaze Forger");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(60)
                .text("This is a Blaze Forger, which functions like an Anvil")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 128);
        scene.world().setKineticSpeed(util.select().position(3, 2, 3), -128);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 3), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 8000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(25);

        scene.overlay().showText(60)
                .text("Pass in Liquid Experience to get it started")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().position(1, 1, 2), 128);
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.getNormalTank().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(50);

        scene.overlay().showText(80)
                .text("Blaze Forger can merge enchantments of the same item like an anvil, and there is no \"repair cost\"")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(85);
        var sword1 = Items.DIAMOND_SWORD.getDefaultInstance();
        var sword2 = Items.DIAMOND_SWORD.getDefaultInstance();
        CEIPonderScenes.enchant(scene, sword1, Enchantments.SWEEPING_EDGE, 1);
        CEIPonderScenes.enchant(scene, sword2, Enchantments.SWEEPING_EDGE, 1);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword1, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword2, false));
        scene.idle(90);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Blaze Forger can also merge Enchanting Templates")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(65);
        var template1 = CEIItems.ENCHANTING_TEMPLATE.asStack();
        var template2 = CEIItems.ENCHANTING_TEMPLATE.asStack();
        CEIPonderScenes.enchant(scene, template1, Enchantments.SWEEPING_EDGE, 1);
        CEIPonderScenes.enchant(scene, template2, Enchantments.SWEEPING_EDGE, 1);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template1, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template2, false));
        scene.idle(90);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Most importantly, Blaze Forger is able to apply Enchanting Templates to items!")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(65);
        var sword3 = Items.DIAMOND_SWORD.getDefaultInstance();
        var template3 = CEIItems.ENCHANTING_TEMPLATE.asStack();
        CEIPonderScenes.enchant(scene, sword3, Enchantments.SWEEPING_EDGE, 1);
        CEIPonderScenes.enchant(scene, template3, Enchantments.SWEEPING_EDGE, 2);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword3, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template3, false));
        scene.idle(90);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Also, Blaze Forger is able to strip enchantment from equipment, book or Enchanting Template to a blank Enchanting Template!")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(85);
        var sword4 = Items.DIAMOND_SWORD.getDefaultInstance();
        CEIPonderScenes.enchant(scene, sword4, Enchantments.SWEEPING_EDGE, 2);
        CEIPonderScenes.enchant(scene, sword4, Enchantments.BANE_OF_ARTHROPODS, 2);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword4, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(CEIItems.ENCHANTING_TEMPLATE.asStack(), false));
        scene.idle(90);
    }

    public static void superEnchant(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("blaze_forger.super_enchant", "Super Enchant Time!");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(60)
                .colored(PonderPalette.BLUE)
                .text("You may have noticed that the Blaze Forger has two \"stomachs\". Try feeding him some Cake o' Enchanting?")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));

        scene.idle(20);
        scene.overlay().showControls(util.vector().centerOf(2, 2, 1), Pointing.RIGHT, 20).rightClick().withItem(CEIItems.EXPERIENCE_CAKE.asStack());
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000)));
        scene.world().modifyBlock(util.grid().at(2, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.idle(15);

        scene.overlay().showText(40)
                .colored(PonderPalette.RED)
                .text("Look! SEETHING BLAZE!")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(45);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .text("Now it's ready to do something truly fascinating! Your merging can break the enchantment level cap! Conflicting enchantments can be merged together!")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(105);

        scene.overlay().showText(60)
                .text("Oh, as a side note, Super Enchanting Templates require this state to be applied and processed")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(65);

        scene.addKeyframe();
        scene.world().setBlock(util.grid().at(2, 2, 2), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
        scene.overlay().showText(60)
                .text("Don't forget to place a lighting rod. You'll need it")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 2));
        scene.idle(45);
        var sword = Items.DIAMOND_SWORD.getDefaultInstance();
        var template = CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack();
        CEIPonderScenes.enchant(scene, sword, Enchantments.SWEEPING_EDGE, 3);
        CEIPonderScenes.enchant(scene, template, Enchantments.SWEEPING_EDGE, 4);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword, false));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template, false));
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(2, 2, 2)));
            return lightning;
        });
        scene.world().setBlock(util.grid().at(3, 1, 1), CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);
        scene.world().setBlock(util.grid().at(2, 2, 3), CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);

        scene.idle(20);
        scene.overlay().showText(40)
                .text("Super Enchanting can cause lightning strikes")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 2));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.extractItem(false));
        scene.idle(45);

        scene.overlay().showText(80)
                .text("You can cover the Blaze Forger with block to avoid lightning strikes, but then \"repair cost\" may appear")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.scaleSceneView(.8f);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 4, 1), Blocks.NETHERITE_BLOCK.defaultBlockState(), false);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 5, 1), Blocks.NETHERITE_BLOCK.defaultBlockState(), false);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 6, 1), Blocks.NETHERITE_BLOCK.defaultBlockState(), false);
        scene.idle(25);

        scene.overlay().showText(80)
                .text("You don't want to do anything? Emmmm okay...")
                .attachKeyFrame()
                .independent();
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 6, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 5, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 4, 1), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().setBlock(util.grid().at(2, 2, 2), Blocks.AIR.defaultBlockState(), true);
        scene.idle(5);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> {
                    be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000));
                    be.insertItem(sword, false);
                });
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(sword, false));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), BlazeForgerBlockEntity.class,
                be -> be.insertItem(template, false));
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(2, 2, 1)));
            return lightning;
        });
        scene.world().setBlock(util.grid().at(2, 2, 1), AllBlocks.LIT_BLAZE_BURNER.getDefaultState(), false);
        scene.idle(20);
    }
}
