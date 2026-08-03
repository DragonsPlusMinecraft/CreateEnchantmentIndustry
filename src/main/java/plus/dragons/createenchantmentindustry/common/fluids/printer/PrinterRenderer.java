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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import static plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity.PROCESSING_TIME;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;

public class PrinterRenderer extends SmartBlockEntityRenderer<PrinterBlockEntity> {
    private static final int PISTON_MOVING_TIME = 5;
    private static final PartialModel[] NOZZLE = {
            CEIPartialModels.PRINTER_NOZZLE_TOP,
            CEIPartialModels.PRINTER_NOZZLE_BOTTOM
    };

    public PrinterRenderer(Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PrinterBlockEntity printer, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(printer, partialTicks, poseStack, buffer, light, overlay);
        TankSegment tank = printer.tank.getPrimaryTank();
        FluidStack fluidStack = tank.getRenderedFluid();
        float fluidLevel = tank.getFluidLevel().getValue(partialTicks);
        if (!fluidStack.isEmpty() && fluidLevel != 0) {
            boolean top = fluidStack.getFluid().getFluidType().isLighterThanAir();
            fluidLevel = Math.max(fluidLevel, 0.175f) * (11 / 16f);
            float min = 2.5f / 16f;
            float max = min + (11 / 16f);
            float minY = top ? (max - fluidLevel) : min;
            float maxY = top ? max : (min + fluidLevel);
            ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack,
                    min, minY, min,
                    max, maxY, max,
                    buffer, poseStack, light,
                    false, true);
        }

        float progress = getProgress(printer.processingTicks - partialTicks);

        BlockState state = printer.getBlockState();
        poseStack.pushPose();
        for (PartialModel nozzle : NOZZLE) {
            poseStack.translate(0, 3 * progress / 32f, 0);
            CachedBuffers.partial(nozzle, state)
                    .light(light)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
        }
        poseStack.popPose();

        CachedBuffers.partial(CEIPartialModels.PRINTER_PISTON, state)
                .translate(0, -progress / 2f, 0)
                .light(light)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
    }

    public static float getProgress(float partialTicks) {
        if (partialTicks < 0) {
            return 0;
        } else if (partialTicks < PISTON_MOVING_TIME) {
            return Mth.lerp(partialTicks / PISTON_MOVING_TIME, 0, 1);
        } else if (partialTicks < PROCESSING_TIME - PISTON_MOVING_TIME) {
            return 1;
        } else if (partialTicks < PROCESSING_TIME) {
            return Mth.lerp((PROCESSING_TIME - partialTicks) / PISTON_MOVING_TIME, 0, 1);
        }
        return 0;
    }
}
