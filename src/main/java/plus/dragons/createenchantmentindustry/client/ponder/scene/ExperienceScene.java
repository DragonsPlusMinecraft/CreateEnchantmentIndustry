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
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public class ExperienceScene {
    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.intro", "Introduction to Experience Handling");
        scene.configureBasePlate(0, 0, 12);
        scene.scaleSceneView(.38f);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(11, 1, 9, 9, 7, 11), Direction.DOWN);

        scene.overlay().showText(60)
                .text("This is a tank of Liquid Experience, and it will be your most used form of experience")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(9, 5, 9));
        for (int i = 0; i < 6; i++) {
            scene.world().modifyBlockEntity(util.grid().at(9, 4, 9), FluidTankBlockEntity.class,
                    be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 10000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(10);
        }
        scene.idle(5);

        scene.overlay().showText(60)
                .text("To convert the Experience you carry to liquid form, you will need two components")
                .attachKeyFrame()
                .independent();

        // Show Exp Block
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(11, 1, 6, 10, 1, 8), Direction.DOWN);
        scene.world().showSection(util.select().position(9, 1, 8), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(8, 1, 9, 6, 1, 11), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(11, 2, 7, 10, 2, 8), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(8, 2, 10, 7, 2, 11), Direction.DOWN);
        scene.idle(25);

        // Two Hatches
        var liquidHatch = util.select().position(11, 4, 8);
        scene.world().showSection(liquidHatch, Direction.SOUTH);
        scene.idle(5);
        scene.overlay().showText(40)
                .text("Liquid Hatch for accessing liquids directly from items")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(11, 4, 8));
        scene.overlay().showOutline(PonderPalette.GREEN, liquidHatch, liquidHatch, 40);
        scene.idle(45);

        var expHatch = util.select().position(8, 4, 11);
        scene.world().showSection(expHatch, Direction.EAST);
        scene.idle(5);
        scene.overlay().showText(40)
                .text("Experience Hatch for accessing experience directly from the player")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(8, 4, 11));
        scene.overlay().showOutline(PonderPalette.GREEN, expHatch, expHatch, 40);
        scene.idle(45);

        scene.overlay().showText(40)
                .text("But as part of Create, automation is a must, right?")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .independent();

        scene.world().showSection(util.select().fromTo(9, 1, 2, 9, 1, 7), Direction.DOWN);
        scene.world().showSection(util.select().position(9, 2, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(8, 1, 2, 8, 1, 4), Direction.DOWN);
        scene.world().showSection(util.select().position(7, 1, 3), Direction.DOWN);
        scene.idle(20);

        // Item Drain
        var itemDrain = util.select().position(9, 2, 2);
        scene.world().showSection(itemDrain, Direction.DOWN);
        scene.idle(25);

        scene.overlay().showText(50)
                .text("You can use Item Drain to pour Liquid Experience out of items")
                .placeNearTarget()
                .pointAt(util.vector().topOf(9, 2, 2));
        scene.overlay().showOutline(PonderPalette.GREEN, itemDrain, itemDrain, 50);

        scene.world().showSection(util.select().fromTo(7, 1, 2, 0, 1, 2), Direction.DOWN);
        scene.world().showSection(util.select().position(8, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(11, 1, 2, 10, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().fromTo(11, 1, 2, 0, 2, 2), -32f);
        scene.idle(5);
        scene.world().createItemOnBelt(util.grid().at(0, 1, 2), Direction.UP, new ItemStack(Items.EXPERIENCE_BOTTLE));
        scene.idle(20);
        scene.world().createItemOnBelt(util.grid().at(0, 1, 2), Direction.UP, new ItemStack(Items.EXPERIENCE_BOTTLE));
        scene.idle(30);

        // GrindStone Drain
        var grindStoneDrain = util.select().fromTo(9, 2, 4, 9, 3, 4);
        scene.world().showSection(grindStoneDrain, Direction.DOWN);
        scene.overlay().showText(60)
                .text("You can also use Mechanical GrindStone to pulverize items like Experience Nuggets into Liquid Experience")
                .placeNearTarget()
                .pointAt(util.vector().topOf(9, 3, 4));
        scene.overlay().showOutline(PonderPalette.GREEN, grindStoneDrain, grindStoneDrain, 60);

        scene.world().showSection(util.select().fromTo(7, 1, 4, 0, 2, 4), Direction.DOWN);
        scene.world().showSection(util.select().position(9, 3, 4), Direction.DOWN);
        scene.world().showSection(util.select().position(8, 2, 4), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 1, 11), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(11, 1, 4, 10, 2, 4), Direction.DOWN);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().fromTo(11, 1, 4, 3, 2, 4), -64f);
        scene.world().setKineticSpeed(util.select().fromTo(2, 1, 4, 0, 1, 4), 64f);
        scene.world().setKineticSpeed(util.select().position(9, 3, 4), 64f);
        scene.world().setKineticSpeed(util.select().fromTo(3, 1, 3, 3, 1, 11), -64f);
        scene.world().setKineticSpeed(util.select().position(9, 1, 7), 48f);
        scene.world().propagatePipeChange(util.grid().at(9, 1, 7));
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 4), BrassTunnelBlockEntity.class,
                be -> be.getBehaviour(SidedFilteringBehaviour.TYPE).setFilter(Direction.EAST, new ItemStack(AllItems.EXP_NUGGET.get())));
        scene.idle(5);
        scene.world().createItemOnBelt(util.grid().at(3, 1, 11), Direction.UP, new ItemStack(AllItems.EXP_NUGGET.get()));
        scene.idle(20);
        scene.world().createItemOnBelt(util.grid().at(3, 1, 11), Direction.UP, new ItemStack(AllItems.EXP_NUGGET.get()));
        scene.idle(20);
        scene.world().createItemOnBelt(util.grid().at(3, 1, 11), Direction.UP, new ItemStack(AllItems.EXP_NUGGET.get()));
        scene.idle(10);

        // Crushing Wheel
        scene.world().showSection(util.select().column(2, 11), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(4, 1, 11, 4, 3, 11), Direction.DOWN);
        scene.idle(5);
        var crushingWheel = util.select().fromTo(5, 2, 10, 1, 4, 10);
        scene.world().showSection(crushingWheel, Direction.NORTH);
        scene.overlay().showText(60)
                .text("Crushing Wheel now has a new mechanism: It has a chance of dropping a very small amount of experience nugget when it kills a creature")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(3, 3, 10));
        scene.overlay().showOutline(PonderPalette.GREEN, crushingWheel, crushingWheel, 30);

        scene.world().setKineticSpeed(util.select().position(4, 3, 10), 128f);
        scene.world().setKineticSpeed(util.select().position(2, 3, 10), -128f);
        scene.idle(30);

        ElementLink<EntityElement> sheep = scene.world().createEntity(w -> {
            Sheep entity = EntityType.SHEEP.create(w);
            entity.setColor(DyeColor.PINK);
            Vec3 p = util.vector().topOf(util.grid().at(3, 3, 10));
            entity.setPos(p.x, p.y, p.z);
            entity.xo = p.x;
            entity.yo = p.y;
            entity.zo = p.z;
            WalkAnimationState animation = entity.walkAnimation;
            animation.update(-animation.position(), 1);
            animation.setSpeed(1);
            entity.yRotO = 210;
            entity.setYRot(210);
            entity.yHeadRotO = 210;
            entity.yHeadRot = 210;
            return entity;
        });
        scene.idle(10);
        scene.world().modifyEntity(sheep, Entity::discard);
        scene.effects().emitParticles(util.vector().topOf(util.grid().at(3, 3, 10))
                .add(0, -.25, 0),
                scene.effects().particleEmitterWithinBlockSpace(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.RED_CONCRETE.defaultBlockState()),
                        util.vector().centerOf(0, 0, 0)),
                25, 1);
        ElementLink<EntityElement> itemEntity = scene.world().createItemEntity(util.vector().blockSurface(util.grid().at(3, 2, 10), Direction.DOWN, 0), util.vector().of(0, 0, 0), new ItemStack(Items.PINK_WOOL));
        ElementLink<EntityElement> itemEntity2 = scene.world().createItemEntity(util.vector().blockSurface(util.grid().at(3, 2, 10), Direction.DOWN, 0), util.vector().of(0, 0, 0), new ItemStack(AllItems.EXP_NUGGET.get()));
        scene.idle(13);
        scene.world().modifyEntity(itemEntity, Entity::discard);
        scene.world().createItemOnBelt(util.grid().at(3, 1, 10), Direction.DOWN, new ItemStack(Items.PINK_WOOL));
        scene.idle(10);
        scene.world().modifyEntity(itemEntity2, Entity::discard);
        scene.world().createItemOnBelt(util.grid().at(3, 1, 10), Direction.DOWN, new ItemStack(AllItems.EXP_NUGGET.get()));
        scene.idle(10);

        // Deployer
        scene.world().showSection(util.select().fromTo(6, 1, 6, 4, 1, 8), Direction.DOWN);
        scene.idle(5);
        var deployer = util.select().position(6, 2, 8);
        var deployerPos = util.grid().at(6, 2, 8);
        scene.world().showSection(deployer, Direction.DOWN);
        scene.overlay().showText(60)
                .text("Deployer also has a new mechanism: When mob is killed by deployer, experience nuggets are dropped")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(6, 2, 8));
        scene.overlay().showOutline(PonderPalette.GREEN, deployer, deployer, 30);
        scene.idle(5);
        scene.world().showSection(util.select().position(5, 2, 8), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().fromTo(5, 1, 8, 4, 2, 8), 64f);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(7, 2, 5, 5, 3, 7), Direction.DOWN);
        scene.world().showSection(util.select().position(5, 3, 8), Direction.DOWN);
        scene.world().showSection(util.select().column(7, 8), Direction.DOWN);
        scene.idle(5);

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        scene.idle(5);
        scene.world().modifyBlockEntityNBT(deployer, DeployerBlockEntity.class, nbt -> {
            nbt.put("HeldItem", sword.saveOptional(scene.world().getHolderLookupProvider()));
            nbt.putString("mode", "PUNCH");
        });
        scene.idle(5);
        scene.world().setKineticSpeed(deployer, 32f);

        ElementLink<EntityElement> sheep2 = scene.world().createEntity(w -> {
            Sheep entity = EntityType.SHEEP.create(w);
            entity.setColor(DyeColor.PINK);
            Vec3 p = util.vector().topOf(util.grid().at(6, 1, 6));
            entity.setPos(p.x, p.y, p.z);
            entity.xo = p.x;
            entity.yo = p.y;
            entity.zo = p.z;
            WalkAnimationState animation = entity.walkAnimation;
            animation.update(-animation.position(), 1);
            animation.setSpeed(1);
            entity.yRotO = 210;
            entity.setYRot(210);
            entity.yHeadRotO = 210;
            entity.yHeadRot = 210;
            return entity;
        });
        scene.idle(5);
        scene.world().moveDeployer(deployerPos, 1, 25);
        scene.idle(26);
        scene.world().modifyEntity(sheep2, Entity::discard);
        scene.effects().emitParticles(util.vector().topOf(deployerPos.north(2))
                .add(0, -.25, 0),
                scene.effects().particleEmitterWithinBlockSpace(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PINK_WOOL.defaultBlockState()),
                        util.vector().of(0, 0, 0)),
                25, 1);
        scene.world().moveDeployer(deployerPos, -1, 25);

        scene.world().flapFunnel(deployerPos.west(), true);
        scene.world().createItemOnBelt(deployerPos.west().below(), Direction.SOUTH, new ItemStack(AllItems.EXP_NUGGET.get()));
        scene.idle(20);

        scene.world().flapFunnel(deployerPos.west(), true);
        scene.world().createItemOnBelt(deployerPos.west().below(), Direction.SOUTH, new ItemStack(Items.MUTTON));
        scene.idle(20);

        scene.world().flapFunnel(deployerPos.west(), true);
        scene.world().createItemOnBelt(deployerPos.west().below(), Direction.SOUTH, new ItemStack(Items.PINK_WOOL));
        scene.idle(20);

        // spout
        scene.world().showSection(util.select().fromTo(11, 7, 0, 11, 7, 8), Direction.SOUTH);
        scene.world().showSection(util.select().fromTo(11, 2, 0, 2, 7, 0), Direction.SOUTH);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().position(6, 2, 0), 256f);
        scene.world().setKineticSpeed(util.select().position(11, 7, 2), 256f);
        scene.world().propagatePipeChange(util.grid().at(6, 2, 0));
        scene.world().propagatePipeChange(util.grid().at(11, 7, 2));
        var spout = util.select().position(2, 3, 0);
        scene.overlay().showText(60)
                .text("If you need to refill Liquid Experience into a container item, use spout")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 3, 0));
        scene.overlay().showOutline(PonderPalette.GREEN, spout, spout, 40);
        scene.world().showSection(util.select().fromTo(11, 1, 0, 0, 1, 0), Direction.SOUTH);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().fromTo(11, 1, 0, 0, 1, 0), 16f);
        scene.idle(5);
        scene.world().createItemOnBelt(util.grid().at(11, 1, 0), Direction.DOWN, new ItemStack(Items.GLASS_BOTTLE));
        scene.idle(20);
        scene.world().createItemOnBelt(util.grid().at(11, 1, 0), Direction.DOWN, new ItemStack(Items.BUCKET));
        scene.idle(20);
        scene.world().createItemOnBelt(util.grid().at(11, 1, 0), Direction.DOWN, new ItemStack(CEIItems.EXPERIENCE_CAKE_BASE.get()));
        scene.idle(20);

        // leak
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(4, 7, 9, 8, 7, 11), Direction.EAST);
        scene.world().setKineticSpeed(util.select().position(8, 7, 11), 256f);
        scene.world().propagatePipeChange(util.grid().at(8, 7, 11));
        scene.idle(40);
        scene.overlay().showText(60)
                .text("Don't worry, leaked Liquid Experience will turn into experience orbs")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(4, 7, 9));
        scene.idle(65);
    }

    public static void advance(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.advance", "Things you might want to know");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().position(4, 1, 0), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(50)
                .text("You need to know a few more things before you're ready to start working on enchantments")
                .placeNearTarget()
                .pointAt(util.vector().topOf(4, 1, 0));
        for (int i = 0; i <= 4; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i == 0 && j == 4) continue;
                scene.world().showSection(util.select().position(j, 1, i), Direction.DOWN);
                scene.idle(2);
            }
        }
        scene.idle(5);
        scene.overlay().showText(55)
                .text("Block of Experience is no longer purely decorative and storage block. You'll need it later")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 0));
        scene.overlay().showOutline(PonderPalette.GREEN, util.select().fromTo(4, 1, 0, 0, 1, 1), util.select().fromTo(4, 1, 0, 0, 1, 1), 55);
        scene.idle(60);

        scene.overlay().showText(55)
                .text("Block of Experience is required to make both the Enchantment Template and the Block of Super Experience")
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 0));
        scene.idle(40);

        for (int i = 3; i <= 4; i++) {
            for (int j = 4; j >= 0; j--) {
                scene.world().showSection(util.select().position(j, 2, i), Direction.DOWN);
                scene.idle(2);
            }
        }

        scene.overlay().showText(55)
                .text("This is Block of Super Experience. You'll need it for making Super Enchanting Template")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 3));
        scene.overlay().showOutline(PonderPalette.BLUE, util.select().fromTo(4, 1, 3, 0, 2, 4), util.select().fromTo(4, 1, 3, 0, 2, 4), 40);
        scene.idle(60);

        scene.world().showSection(util.select().position(3, 2, 1), Direction.DOWN);
        scene.world().showSection(util.select().position(3, 3, 3), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(40)
                .text("Blaze Enchanter")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 2, 1));
        scene.world().modifyBlock(util.grid().at(3, 3, 3), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.overlay().showOutline(PonderPalette.GREEN, util.select().position(3, 2, 1), util.select().position(3, 2, 1), 40);
        scene.idle(45);

        scene.world().showSection(util.select().position(1, 2, 1), Direction.DOWN);
        scene.world().showSection(util.select().position(1, 3, 3), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(40)
                .text("Blaze Forger")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.world().modifyBlock(util.grid().at(1, 3, 3), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.overlay().showOutline(PonderPalette.GREEN, util.select().position(1, 2, 1), util.select().position(1, 2, 1), 40);
        scene.idle(45);

        scene.world().modifyBlock(util.grid().at(3, 3, 3), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.world().modifyBlock(util.grid().at(3, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(5);
        scene.world().modifyBlock(util.grid().at(1, 3, 3), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING), false);
        scene.world().modifyBlock(util.grid().at(1, 2, 1), bs -> bs.setValue(BlazeBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(5);
        scene.overlay().showText(55)
                .text("There are Blaze on Seething mode, which means they're in Super Enchanting mode")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(3, 3, 3));
        scene.overlay().showOutline(PonderPalette.BLUE, util.select().position(3, 3, 3), util.select().position(3, 3, 3), 40);
        scene.overlay().showOutline(PonderPalette.BLUE, util.select().position(1, 3, 3), util.select().position(1, 3, 3), 40);
        scene.idle(60);

        scene.overlay().showText(40)
                .text("Super Enchanting allows you to break the limit!")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 3, 3));
        scene.idle(40);

        scene.overlay().showText(85)
                .text("Super Enchanting specifically includes exceeding enchantment level cap, merging conflicting enchantments, and obtaining treasure enchantments directly")
                .placeNearTarget()
                .pointAt(util.vector().topOf(3, 3, 3));
        scene.idle(90);
    }

    public static void prepare(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience.prepare_for_super_enchant", "Prepare materials for Super Enchant");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(2, 1, 0, 0, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(80)
                .text("First material is Block of Super Experience, which will be used to make the Super Enchanting Template. This requires the use of Block of Experience")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(80);
        scene.overlay().showText(45)
                .text("And you're supposed to use a lightning rod")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.world().showSection(util.select().position(1, 2, 1), Direction.DOWN);
        scene.idle(50);
        scene.world().createEntity(level -> {
            var lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(util.grid().at(1, 2, 1)));
            return lightning;
        });
        scene.world().replaceBlocks(util.select().layer(1), CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState(), false);
        scene.idle(5);
        scene.overlay().showText(45)
                .text("Lighting Strike!")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.idle(50);

        scene.overlay().showText(80)
                .text("The vast majority of lightning strikes only have a certain probability of transforming Block of Experience, and only lightning strikes caused by Super Enchanting are guaranteed to transform")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 2, 1));
        scene.idle(85);

        scene.world().replaceBlocks(util.select().position(1, 2, 1), Blocks.AIR.defaultBlockState(), true);
        scene.world().replaceBlocks(util.select().layer(1), Blocks.AIR.defaultBlockState(), true);
        scene.world().hideSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.idle(20);
        scene.world().setBlock(util.grid().at(1, 1, 1), AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), DepotBlockEntity.class, be -> be.setHeldItem(CEIItems.EXPERIENCE_CAKE.asStack()));
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Second material is Cake o' Enchanting. It works as \"Super Experience\" to support blaze seething mode. Blaze will love eating this")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(60);
        scene.overlay().showText(80)
                .text("You might be a little confused, if Cake o' Enchanting is used to put Blaze into Super Enchanting mode, what is Block of Super Experience used for?")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(65);
        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), DepotBlockEntity.class, be -> be.setHeldItem(CEIItems.SUPER_ENCHANTING_TEMPLATE.asStack()));
        scene.idle(20);
        scene.overlay().showText(45)
                .text("The answer is used to make our third item: Super Enchanting Template")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(50);
        scene.overlay().showText(75)
                .text("In Super Enchanting mode, Blaze will not accept any normal Enchanting Templates and must use the Super Enchanting Template")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(80);
    }
}
