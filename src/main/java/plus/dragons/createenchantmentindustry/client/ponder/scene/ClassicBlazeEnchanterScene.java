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

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
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
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicEnchanterBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public class ClassicBlazeEnchanterScene {
    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("classic_blaze_enchanter.intro", "Introduction to Classic Blaze Enchanter");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(70)
                .text("This is a Classic Blaze Enchanter. It continuously enchants items according to designated Enchanting Template")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Provide it Liquid Experience to activate it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().position(4, 1, 3), 256.0F);
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 2), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 64000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), ClassicBlazeEnchanterBlockEntity.class,
                be -> be.getNormalTank().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000), IFluidHandler.FluidAction.EXECUTE));
        scene.world().modifyBlock(util.grid().at(2, 2, 1),
                bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().position(0, 1, 3), 256.0F);
        scene.idle(40);

        Vec3 slotVec = util.vector().of(2.0f, 2.5f, 1.5f);
        scene.overlay().showFilterSlotInput(slotVec, Direction.WEST, 40);
        scene.overlay().showText(75)
                .text("Before enchanting with it, Enchanting Template must be set via filter slot")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(slotVec);
        scene.idle(10);
        var template = CEIItems.ENCHANTING_TEMPLATE.asStack();
        CEIPonderScenes.enchant(scene, template, Enchantments.SWEEPING_EDGE, 1);
        CEIPonderScenes.enchant(scene, template, Enchantments.BANE_OF_ARTHROPODS, 1);
        scene.overlay().showControls(slotVec, Pointing.RIGHT, 40).withItem(template).rightClick();
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), ClassicBlazeEnchanterBlockEntity.class, be -> {
            ClassicEnchanterBehaviour enchanter = be.getBehaviour(ClassicEnchanterBehaviour.TYPE);
            enchanter.setFilter(template);
        });
        scene.idle(75);

        scene.overlay().showText(100)
                .text("Enchantment on the Enchanting Template will be enchanted to item and will not consume the Enchanting Template or the enchantment")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(slotVec);
        scene.idle(110);

        scene.overlay().showText(70)
                .text("If multiple enchantments are applicable, random one will be applied")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(slotVec);
        scene.idle(25);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), ClassicBlazeEnchanterBlockEntity.class, be -> be.insertItem(Items.DIAMOND_SWORD.getDefaultInstance(), false));
        scene.idle(55);

        scene.overlay().showText(70)
                .text("Classic Blaze Enchanter enters Super Enchanting mode in same way as Blaze Enchanter, requiring Cake O' Enchanting")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), ClassicBlazeEnchanterBlockEntity.class, be -> {
            be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000));
            be.extractItem(false, false);
        });
        scene.world().modifyBlock(util.grid().at(2, 2, 1),
                bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.idle(70);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("Classic Blaze Enchanter in Super Enchanting mode exclusively requires Super Enchanting Templates")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 2, 1));
        var template2 = CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack();
        CEIPonderScenes.enchant(scene, template2, Enchantments.SMITE, 5);
        scene.overlay().showControls(slotVec, Pointing.RIGHT, 45).withItem(template2).rightClick();
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), ClassicBlazeEnchanterBlockEntity.class, be -> {
            ClassicEnchanterBehaviour enchanter = be.getBehaviour(ClassicEnchanterBehaviour.TYPE);
            enchanter.setFilter(template2);
        });
        scene.idle(70);

        scene.overlay().showText(80)
                .text("If the item already has an enchantment of the same type and level, Super Enchanting increases the enchantment by one level and can surpass vanilla enchantment level cap")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(90);

        scene.overlay().showText(40)
                .text("Make sure to place a Lightning Rod nearby")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 1), ClassicBlazeEnchanterBlockEntity.class, be -> be.insertItem(Items.DIAMOND_SWORD.getDefaultInstance(), false));
        scene.world().setBlock(util.grid().at(1, 2, 1), Blocks.LIGHTNING_ROD.defaultBlockState(), true);
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(1, 2, 1)));
            return lightning;
        });
        scene.world().setBlock(util.grid().at(1, 1, 1), CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);
        scene.overlay().showText(40)
                .text("Super Enchanting can cause lightning strikes")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(1, 2, 1)));
            return lightning;
        });
        scene.idle(10);
        scene.overlay().showText(80)
                .text("You can cover the Classic Blaze Enchanter with block to avoid lightning strikes, but then level of existing enchantment may drop")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(80);
    }

    public static void automate(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("classic_blaze_enchanter.automate", "Automating");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        var inputPos = util.grid().at(4, 1, 1);
        var input = util.select().position(4, 1, 1);
        var armPos = util.grid().at(4, 1, 3);
        var arm = util.select().position(4, 1, 3);
        var enchanter = util.select().position(2, 1, 2);
        scene.world().showSection(enchanter.add(arm).add(input), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(100)
                .text("Classic Blaze Enchanter can be automated with Mechanical Arm. It can insert item for enchanting and feed experience fuel")
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(110);

        var belt = util.select().fromTo(2, 1, 0, 2, 1, 4).add(util.select().fromTo(0, 1, 2, 1, 1, 2)).substract(enchanter);
        scene.world().showSection(belt, Direction.DOWN);
        scene.world().setKineticSpeed(belt, 64);
        scene.overlay().showText(60)
                .text("Classic Blaze Enchanter also can be automated with Belt")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Experience fuel cannot be fed via belt")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(10);
        scene.world().setKineticSpeed(arm, 128);
        scene.world().modifyBlockEntity(inputPos, DepotBlockEntity.class,
                depot -> depot.setHeldItem(CEIItems.EXPERIENCE_CAKE.asStack()));
        scene.idle(10);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(inputPos, DepotBlockEntity.class, depot -> depot.setHeldItem(ItemStack.EMPTY));
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_OUTPUTS, CEIItems.EXPERIENCE_CAKE.asStack(), -1);
        scene.idle(20);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, CEIItems.EXPERIENCE_CAKE.asStack(), 0);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), ClassicBlazeEnchanterBlockEntity.class,
                be -> be.getSpecialTank().setFluid(new FluidStack(CEIFluids.EXPERIENCE.get(), 4000)));
        scene.world().modifyBlock(util.grid().at(2, 1, 2), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, -1);
        scene.idle(20);
    }
}
