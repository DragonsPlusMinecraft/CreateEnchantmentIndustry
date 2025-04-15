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
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

public class MiscScene {
    public static void experienceHatch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("experience_hatch.intro", "Introduction to Experience Hatch");
        scene.configureBasePlate(0, 0, 4);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 1, 1, 1, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(3, 2, 2, 2, 3, 3), Direction.DOWN);
        scene.world().showSection(util.select().position(1, 3, 2), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(50)
                .text("It is very simple to use. Right click Hatch to store Experience...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        var frontVec = util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST)
                .add(-.125, 0, 0);
        scene.overlay().showControls(frontVec, Pointing.UP, 50).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 10000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(40);

        scene.world().modifyBlockEntity(util.grid().at(3, 2, 1), BasinBlockEntity.class,
                be -> be.inputTank.getPrimaryHandler().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(3, 2, 1, 2, 2, 1), Direction.UP);
        scene.idle(5);
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
        scene.idle(30);

        scene.overlay().showText(55)
                .text("There are a filter slot and a scroll panel on Hatch. You can configure how much Experience per interaction on the panel")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        for (int i = 0; i < 12; i++) {
            scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                    be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(5);
        }

        scene.overlay().showText(40)
                .text("The filter slot is used to deal with experience fluids of other mods")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        scene.idle(30);
        scene.world().showSection(util.select().position(0, 1, 1), Direction.DOWN);
        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).get(), 36000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.overlay().showText(40)
                .text("Let's assume that Liquid Cyan Dye is experience fluid from another mod...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        scene.idle(40);
        scene.overlay().showText(40)
                .text("...And place Bucket Cyan Dye in filter slot")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        scene.overlay().showControls(util.vector().centerOf(0, 1, 1), Pointing.DOWN, 40).withItem(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).getBucket().get().getDefaultInstance());
        scene.idle(40);

        scene.overlay().showText(60)
                .text("Now, you can directly save and extract your experience as 'The Cyan Experience'!")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        for (int i = 0; i < 12; i++) {
            scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                    be -> be.getControllerBE().getTankInventory().drain(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).get(), 3000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(5);
        }
    }

    public static void printer(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("printer.intro", "Introduction to Printer");
        scene.configureBasePlate(1, 1, 3);
        scene.world().showSection(util.select().fromTo(1, 0, 1, 3, 0, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(40)
                .text("This is a Printer")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(45);

        var slotVec = util.vector().of(2, 3.5, 2.5);
        scene.overlay().showFilterSlotInput(slotVec, Direction.WEST, 80);
        scene.overlay().showText(80)
                .text("Before you can put it to use, you need to set what you want to print via the filter slot...")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class, be -> {
            var printer = be.getBehaviour(PrinterBehaviour.TYPE);
            var packageItem = CDPItems.RARE_MARBLE_GATE_PACKAGE.asStack();
            printer.setFilter(packageItem);
        });
        scene.idle(85);

        scene.overlay().showText(80)
                .text("...and pass in the corresponding fluid")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(10);
        scene.world().showSection(util.select().column(4, 2), Direction.WEST);
        scene.world().showSection(util.select().position(4, 0, 2), Direction.WEST);
        scene.world().showSection(util.select().position(3, 3, 2), Direction.WEST);
        scene.idle(20);
        scene.world().setKineticSpeed(util.select().position(3, 3, 2), 128f);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class,
                be -> be.getFluidHandler(null).fill(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.BLACK), 3000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(35);

        scene.overlay().showText(80)
                .text("You can always use JEI or another recipe book mod to check all printing recipes")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .independent();
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(new ItemStack(PackageStyles.ALL_BOXES.get(1))));
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), PrinterBlockEntity.class,
                be -> be.processingTicks = 50);
        scene.idle(45);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), DepotBlockEntity.class,
                be -> be.setHeldItem(CDPItems.RARE_MARBLE_GATE_PACKAGE.asStack()));
        scene.idle(50);
    }
}
