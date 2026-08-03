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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.ponder.scene;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.math.Pointing;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusionStats;

public class ApothicEnchantingScene {
    public static void infuser(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("infuser", "Infusing item with Infuser");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        var infuser = util.select().position(2, 3, 2);
        var infuserPos = util.grid().at(2, 3, 2);
        scene.world().modifyBlockEntity(infuserPos, InfuserBlockEntity.class,
                be -> be.setInfusionStats(new InfusionStats(82, 45, 42)));
        scene.world().showSection(infuser, Direction.SOUTH);
        scene.idle(5);

        var infuserVec = util.vector().centerOf(2, 3, 2);
        scene.overlay().showText(60)
                .text("With a Infuser and Basin, Infusion recipes can be automated")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(infuserVec);
        scene.idle(70);

        scene.overlay().showText(70)
                .text("Just like infusing at an Enchantment Table, Infuser also requires bookshelf support")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(infuserVec);
        scene.idle(10);
        scene.world().showSection(util.select().position(4, 1, 0), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 1, 1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 1, 3), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 1, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(3, 1, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(1, 1, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 1, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 1, 3), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 1, 1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 1, 0), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 2, 0), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 2, 1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 2, 3), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 2, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(1, 2, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(3, 2, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 2, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 2, 3), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 2, 1), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 2, 0), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(4, 3, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 3, 4), Direction.DOWN);
        scene.idle(7);

        scene.overlay().showText(80)
                .text("Enchant Table consumes EXP for infusing. Similarly, Infuser consumes Liquid Experience")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(infuserVec);
        scene.idle(10);
        scene.world().modifyBlockEntity(infuserPos, InfuserBlockEntity.class,
                be -> be.getFluidHandler(null).fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 3000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(80);

        var carrot = Items.CARROT.getDefaultInstance();
        var goldCarrot = Items.GOLDEN_CARROT.getDefaultInstance();
        BlockPos basin = util.grid().at(2, 1, 2);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 30).withItem(carrot);
        scene.idle(10);
        scene.world().createItemOnBeltLike(basin, Direction.UP, carrot);
        scene.world().modifyBlockEntity(infuserPos, InfuserBlockEntity.class, InfuserBlockEntity::startProcessingBasin);
        scene.idle(80);
        scene.world().modifyBlockEntityNBT(util.select().position(basin), BasinBlockEntity.class, nbt -> {
            nbt.put("VisualizedItems",
                    NBTHelper.writeCompoundList(
                            ImmutableList.of(IntAttached.with(1, goldCarrot)),
                            ia -> ia.getValue().save(new CompoundTag())));
        });  // Does this really necessary?
        scene.idle(4);
        scene.world().createItemOnBeltLike(util.grid().at(2, 0, 1), Direction.UP, goldCarrot);
        scene.idle(30);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(4, 3, 0), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(0, 3, 0), Direction.DOWN);
        scene.idle(7);
        scene.overlay().showText(60)
                .text("Infuser can safely ignore the Apotheotic Stats max requirements in Infusing Recipes")
                .placeNearTarget()
                .pointAt(infuserVec);
        scene.idle(10);
        scene.world().modifyBlockEntity(infuserPos, InfuserBlockEntity.class,
                be -> be.setInfusionStats(new InfusionStats(22, 100, 42)));
        scene.idle(60);

        scene.overlay().showText(80)
                .text("Please note that Infuser experiences a slight delay in reflecting changes to Apotheotic Stats and will not be affected during processing")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(infuserVec);
        scene.idle(10);
        scene.world().hideSection(util.select().position(4, 3, 0), Direction.UP);
        scene.idle(3);
        scene.world().hideSection(util.select().position(0, 3, 0), Direction.UP);
        scene.idle(3);
        scene.world().hideSection(util.select().position(0, 3, 4), Direction.UP);
        scene.idle(3);
        scene.world().hideSection(util.select().position(4, 3, 4), Direction.UP);
        scene.idle(11);
        scene.world().modifyBlockEntity(infuserPos, InfuserBlockEntity.class,
                be -> be.setInfusionStats(new InfusionStats(42, 45, 42)));
        scene.idle(10);
        scene.world().hideSection(util.select().layer(2), Direction.UP);
        scene.idle(20);
        scene.world().modifyBlockEntity(infuserPos, InfuserBlockEntity.class,
                be -> be.setInfusionStats(new InfusionStats(0, 15, 0)));
        scene.idle(20);
    }

    public static void brassBookshelf(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("brass_bookshelf", "Adjusting Apotheotic Stats with Brass Bookshelf");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0).substract(util.select().position(5, 0, 2)), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 3).add(util.select().position(3, 1, 3)), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 2, 3).add(util.select().position(3, 3, 3)), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(60)
                .text("Both Enchantment Table and Infuser require Apotheotic Stats to function")
                .placeNearTarget()
                .attachKeyFrame()
                .independent();
        scene.idle(70);

        ElementLink<WorldSectionElement> base = scene.world().showIndependentSection(util.select().position(2, 3, 1), Direction.DOWN);
        scene.world().moveSection(base, util.vector().of(0, -2, 0), 0);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 2, 1), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(60)
                .text("Brass Bookshelf provides Apotheosis Stats more efficiently and flexibly")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Adjust the type of Apotheotic Stats provided via top panel of the bookshelf")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(2, 2, 1));
        scene.idle(10);
        scene.overlay().showCenteredScrollInput(util.grid().at(2, 2, 1), Direction.UP, 50);
        scene.idle(60);

        scene.overlay().showText(60)
                .text("Adjust the value of Apotheotic Stats provided via side panels of the bookshelf")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 1), Direction.NORTH));
        scene.idle(10);
        scene.overlay().showCenteredScrollInput(util.grid().at(2, 2, 1), Direction.NORTH, 50);
        scene.idle(60);

        scene.addKeyframe();
        scene.world().hideIndependentSection(base, Direction.SOUTH);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(2, 1, 1, 5, 1, 1), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(5, 0, 2), Direction.WEST);
        scene.idle(5);
        scene.overlay().showText(60)
                .text("Brass bookshelf requires kinetic stress to function")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 1));
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().position(5, 0, 2), 16);
        scene.world().setKineticSpeed(util.select().fromTo(2, 1, 1, 5, 1, 1), -32);
        scene.idle(55);
    }

    public static void creativeBookshelf(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("creative_bookshelf", "The Creative Bookshelf");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.idle(5);

        var shelf = util.vector().centerOf(1, 1, 1);
        scene.overlay().showText(80)
                .text("Creative Bookshelf can provide Apotheotic Stats freely. It provides Eterna, Arcana & Quanta at same time")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(shelf);
        scene.idle(90);

        scene.overlay().showText(60)
                .text("Adjust the value of Apotheotic Stats provided via side panels of the bookshelf")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH));
        scene.idle(10);
        scene.overlay().showCenteredScrollInput(util.grid().at(1, 1, 1), Direction.NORTH, 50);
        scene.idle(60);

        scene.overlay().showText(60)
                .text("Configure the type of Apotheotic Stat value adjusted by the side panels via the top panel of the bookshelf")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(1, 1, 1));
        scene.idle(10);
        scene.overlay().showCenteredScrollInput(util.grid().at(1, 1, 1), Direction.UP, 50);
        scene.idle(60);
    }

    public static void enderWovenBag(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("ender_woven_bag", "Capturing mob with Ender Woven Bag");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        var bag = scene.world().showIndependentSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(60)
                .text("This is an Ender Weave Bag, an Appliance crafted from Ender Leads")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(70);

        scene.world().moveSection(bag, new Vec3(-1, 0, 1), 10);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("It will pull nearby mobs toward it, and capture them")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(1, 1, 3));
        scene.idle(10);
        var pos = Vec3.atBottomCenterOf(util.grid().at(3, 1, 1));
        var sheep = scene.world().createEntity(level -> {
            Sheep s = new Sheep(EntityType.SHEEP, level);
            s.setColor(DyeColor.WHITE);
            Vec3 p = pos;
            s.setPos(p.x, p.y, p.z);
            s.xo = p.x;
            s.yo = p.y;
            s.zo = p.z;
            s.yRotO = 210;
            s.setYRot(210);
            s.yHeadRotO = 210;
            s.yHeadRot = 210;
            return s;
        });
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            scene.world().modifyEntity(sheep, entity -> {
                Vec3 p = pos.add(finalI * -0.05, 0, finalI * 0.05);
                entity.setPos(p.x, p.y, p.z);
            });
            scene.idle(1);
        }
        scene.world().modifyEntity(sheep, Entity::discard);
        scene.idle(40);

        scene.world().moveSection(bag, new Vec3(1, 0, -1), 10);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("When the bag is full, the indicator light glows")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), EnderWovenBagBlockEntity.class, e -> e.pocketOpen = 1);
        scene.idle(60);

        scene.world().showSection(util.select().position(1, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(0, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(60)
                .colored(PonderPalette.RED)
                .text("When powered by Redstone, it releases captured mob")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(10);
        scene.world().toggleRedstonePower(util.select().fromTo(0, 1, 2, 1, 1, 2));
        scene.effects().indicateRedstone(util.grid().at(0, 1, 2));
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), EnderWovenBagBlockEntity.class, e -> e.pocketOpen = 0);
        scene.world().createEntity(level -> {
            Sheep s = new Sheep(EntityType.SHEEP, level);
            s.setColor(DyeColor.WHITE);
            Vec3 p = Vec3.atBottomCenterOf(util.grid().at(2, 1, 1));
            s.setPos(p.x, p.y, p.z);
            s.xo = p.x;
            s.yo = p.y;
            s.zo = p.z;
            WalkAnimationState animation = s.walkAnimation;
            animation.update(-animation.position(), 1);
            animation.setSpeed(1);
            s.yRotO = 210;
            s.setYRot(210);
            s.yHeadRotO = 210;
            s.yHeadRot = 210;
            return s;
        });
        scene.idle(60);
    }

    public static void enderWovenBagOnContraption(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("ender_woven_bag_on_contraption", "Using Ender Woven Bag on Contraption");
        scene.configureBasePlate(1, 0, 9);
        scene.scaleSceneView(0.7f);
        var contraptionSelection = util.select().fromTo(0, 1, 8, 5, 3, 8)
                .add(util.select().position(5, 1, 7));
        scene.world().showSection(util.select().fromTo(1, 0, 0, 9, 0, 8), Direction.DOWN);
        scene.idle(5);
        var gantry = util.select().fromTo(0, 0, 0, 0, 0, 8);
        scene.world().showSection(gantry, Direction.EAST);
        scene.idle(5);
        var contraption = scene.world().showIndependentSection(contraptionSelection, Direction.SOUTH);
        scene.idle(5);

        scene.overlay().showText(60)
                .text("Ender Woven Bag works on Contraption")
                .placeNearTarget()
                .attachKeyFrame()
                .independent();
        scene.idle(10);
        scene.world().setKineticSpeed(gantry, 32);
        scene.world().moveSection(contraption, new Vec3(0, 0, -5), 80);
        scene.idle(10);
        var panda = scene.world().createEntity(level -> {
            Panda entity = new Panda(EntityType.PANDA, level);
            Vec3 p = Vec3.atBottomCenterOf(util.grid().at(5, 1, 1));
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
        scene.idle(70);
        scene.world().modifyEntity(panda, Entity::discard);
        scene.world().setKineticSpeed(gantry, 0);
        scene.idle(10);

        scene.world().setKineticSpeed(gantry, 64);
        scene.world().moveSection(contraption, new Vec3(0, 0, 2), 16);
        scene.idle(16);
        scene.world().setKineticSpeed(gantry, 0);
        scene.world().hideSection(util.select().fromTo(1, 0, 0, 9, 0, 3), Direction.DOWN);
        scene.idle(10);
        var land = scene.world().showIndependentSection(util.select().fromTo(1, 1, 0, 9, 17, 3), Direction.DOWN);
        scene.world().moveSection(land, new Vec3(0, -1, 0), 0);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("When Ender Woven Bag on Contraption faces a Redstone Block, it will release the captured mobs")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(5, 1, 0));
        scene.idle(10);
        scene.world().setKineticSpeed(gantry, -32);
        scene.world().moveSection(contraption, new Vec3(0, 0, -2), 32);
        scene.idle(32);
        scene.world().setKineticSpeed(gantry, 64);
        scene.world().createEntity(level -> {
            Panda entity = new Panda(EntityType.PANDA, level);
            Vec3 p = Vec3.atBottomCenterOf(util.grid().at(5, 1, 1));
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
        scene.world().moveSection(contraption, new Vec3(0, 0, 1), 8);
        scene.idle(8);
        scene.world().setKineticSpeed(gantry, 0);
    }
}
