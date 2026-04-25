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
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import java.util.List;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderScenes;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

public class MiscScene {
    public static void experienceHatch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience_hatch.intro", "Introduction to Experience Hatch");
        scene.configureBasePlate(0, 0, 4);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(3, 1, 1, 1, 1, 3)
                .add(util.select().fromTo(3, 2, 2, 2, 3, 3))
                .add(util.select().position(1, 3, 2)), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(50)
                .text("The Experience Hatch is simple to use. Right click it to store Experience...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        var frontVec = util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST)
                .add(-.125, 0, 0);
        scene.overlay().showControls(frontVec, Pointing.UP, 50).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 10000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(50);

        scene.world().modifyBlockEntity(util.grid().at(3, 2, 1), BasinBlockEntity.class,
                be -> be.inputTank.getPrimaryHandler().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(3, 2, 1, 2, 2, 1), Direction.UP);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("...Shift-Right-Click Hatch to extract stored Experience")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 1));
        frontVec = util.vector().blockSurface(util.grid().at(3, 2, 1), Direction.WEST)
                .add(-.125, 0, 0);
        scene.overlay().showControls(frontVec, Pointing.UP, 50).rightClick().whileSneaking();
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 1), BasinBlockEntity.class,
                be -> be.inputTank.getPrimaryHandler().drain(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(40);

        scene.overlay().showText(80)
                .text("There are a filter slot and a scroll panel on Hatch. You can configure how much Experience is retrieved or deposited per interaction on the panel")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        for (int i = 0; i < 12; i++) {
            scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                                        be -> be.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(5);
        }
        scene.idle(30);

        scene.overlay().showText(40)
                .text("The filter slot is used to deal with experience fluids of other mods")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        scene.idle(30);
        scene.world().showSection(util.select().position(0, 1, 1), Direction.DOWN);
        scene.idle(20);

        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).get(), 36000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.overlay().showText(40)
                .text("For example, assume that Liquid Cyan Dye is experience fluid from another mod")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        scene.idle(50);
        scene.overlay().showText(40)
                .text("Place Bucket of Cyan Dye in the filter slot")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        scene.overlay().showControls(util.vector().centerOf(0, 1, 1), Pointing.DOWN, 40).withItem(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).getBucket().get().getDefaultInstance());
        scene.idle(50);

        scene.overlay().showText(60)
                .text("You can now directly insert and extract Cyan Dye as \"Cyan Experience\"")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        for (int i = 0; i < 12; i++) {
            scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                                        be -> be.getTankInventory().drain(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).get(), 3000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(10);
        }
    }

    public static void printer(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("printer.intro", "Introduction to Printer");
        scene.configureBasePlate(1, 1, 3);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 0, 1, 3, 0, 3)
                .add(util.select().position(2, 1, 2))
                .add(util.select().position(2, 3, 2)), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(40)
                .text("This is a Printer")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(50);

        var slotVec = util.vector().of(2, 3.5, 2.5);
        scene.overlay().showFilterSlotInput(slotVec, Direction.WEST, 80);
        scene.overlay().showText(80)
                .text("Before use, set the item to print via the filter slot...")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST));
        var writtenBook = Items.WRITTEN_BOOK.getDefaultInstance();
        writtenBook.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("1"), "1", 1, List.of(Filterable.passThrough(Component.literal("1"))), true));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class, be -> {
            var printer = be.getBehaviour(PrinterBehaviour.TYPE);
            printer.setFilter(writtenBook);
        });
        scene.idle(90);

        scene.overlay().showText(80)
                .text("...and pass in the corresponding fluid")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(10);
        scene.world().showSection(util.select().column(4, 2)
                .add(util.select().position(4, 0, 2))
                .add(util.select().position(3, 3, 2)), Direction.WEST);
        scene.idle(20);
        scene.world().setKineticSpeed(util.select().position(3, 3, 2), 128f);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class,
                be -> be.getFluidHandler(null).fill(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.BLACK), 3000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(40);

        scene.overlay().showText(80)
                .text("Copying written book")
                .attachKeyFrame()
                .independent();
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(new ItemStack(Items.BOOK)));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class,
                be -> be.processingTicks = 50);
        scene.idle(45);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(Items.WRITTEN_BOOK.getDefaultInstance()));
        scene.idle(50);

        scene.overlay().showText(80)
                .text("Changing package pattern")
                .attachKeyFrame()
                .independent();
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class, be -> {
            var printer = be.getBehaviour(PrinterBehaviour.TYPE);
            var packageItem = CDPItems.RARE_MARBLE_GATE_PACKAGE.asStack();
            printer.setFilter(packageItem);
        });
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(new ItemStack(PackageStyles.ALL_BOXES.get(1))));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class,
                be -> be.processingTicks = 50);
        scene.idle(45);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(CDPItems.RARE_MARBLE_GATE_PACKAGE.asStack()));
        scene.idle(50);

        scene.overlay().showText(80)
                .text("Duplicating Enchanted Book")
                .attachKeyFrame()
                .independent();
        var enchantedBook = Items.ENCHANTED_BOOK.getDefaultInstance();
        CEIPonderScenes.enchant(scene, enchantedBook, Enchantments.CHANNELING, 1);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class, be -> {
            var printer = be.getBehaviour(PrinterBehaviour.TYPE);
            printer.setFilter(enchantedBook);
        });
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class, be -> {
            be.getFluidHandler(null).drain(3000, IFluidHandler.FluidAction.EXECUTE);
            be.getFluidHandler(null).fill(new FluidStack(CEIFluids.EXPERIENCE, 3000), IFluidHandler.FluidAction.EXECUTE);
        });
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(new ItemStack(Items.BOOK)));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class,
                be -> be.processingTicks = 50);
        scene.idle(45);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(Items.ENCHANTED_BOOK.getDefaultInstance()));
        scene.idle(50);

        scene.overlay().showText(80)
                .text("It can also name items, copy train schedule, copy clipboard, change package address and more. Use JEI to look up printing recipe")
                .attachKeyFrame()
                .independent();
        scene.idle(80);
    }

    public static void experienceLantern(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience_lantern.intro", "Introduction to Experience Lantern");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        var lantern = scene.world().showIndependentSection(util.select().position(2, 6, 2), Direction.DOWN);
        scene.world().moveSection(lantern, new Vec3(0, -5, 0), 0);
        scene.idle(10);

        scene.overlay().showText(100)
                .text("Experience Lantern absorbs experience from nearby players and experience orb. It glows according to the amount of experience stored internally")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(100);
        scene.world().hideIndependentSection(lantern, Direction.UP);
        scene.idle(10);

        var contraptionSelection = util.select().fromTo(0, 1, 0, 4, 3, 4);
        scene.world().showSection(util.select().fromTo(2, 4, 2, 2, 5, 2), Direction.DOWN);
        ElementLink<WorldSectionElement> contraption = scene.world().showIndependentSection(contraptionSelection, Direction.DOWN);
        scene.idle(10);

        scene.world().configureCenterOfRotation(contraption, util.vector().centerOf(2, 4, 2));
        scene.overlay().showText(60)
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(util.grid().at(2, 2, 0)))
                .text("Experience Lantern also works on Contraption");
        scene.idle(70);

        scene.world().setKineticSpeed(util.select().fromTo(2, 4, 2, 2, 5, 2), -24);
        scene.world().rotateBearing(util.grid().at(2, 4, 2), -360, 140);
        scene.world().rotateSection(contraption, 0, -360, 0, 140);
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 3), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(40);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 3), FluidTankBlockEntity.class,
                be -> be.getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 2000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(70);
        scene.world().setKineticSpeed(util.select().fromTo(2, 4, 2, 2, 5, 2), 0);
    }
}
