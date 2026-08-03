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

package plus.dragons.createenchantmentindustry.integration.jei.category.printing;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class AnimatedPrinter extends AnimatedKinetics {
    private FluidStack fluid = FluidStack.EMPTY;

    public AnimatedPrinter withFluid(FluidStack fluid) {
        this.fluid = fluid;
        return this;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, 100);
        poseStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        poseStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 20;

        blockElement(CEIBlocks.PRINTER.getDefaultState())
                .scale(scale)
                .render(graphics);

        float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
        float progress = cycle < 20 ? -PrinterRenderer.getProgress(cycle / 20f * 50f) : 0;
        progress *= scale;

        poseStack.pushPose();
        poseStack.translate(0, 3 * progress / 32f, 0);
        blockElement(CEIPartialModels.PRINTER_NOZZLE_TOP)
                .scale(scale)
                .render(graphics);
        poseStack.translate(0, 3 * progress / 32f, 0);
        blockElement(CEIPartialModels.PRINTER_NOZZLE_BOTTOM)
                .scale(scale)
                .render(graphics);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, -progress / 2f, 0);
        blockElement(CEIPartialModels.PRINTER_PISTON)
                .scale(scale)
                .render(graphics);
        poseStack.popPose();

        blockElement(AllBlocks.DEPOT.getDefaultState())
                .atLocal(0, 2, 0)
                .scale(scale)
                .render(graphics);

        if (fluid.isEmpty()) {
            poseStack.popPose();
            return;
        }

        AnimatedKinetics.DEFAULT_LIGHTING.applyLighting();
        poseStack.pushPose();
        UIRenderHelper.flipForGuiRender(poseStack);
        poseStack.scale(16, 16, 16);
        float from = 3f / 16f;
        float to = 17f / 16f;
        ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid,
                from, from, from,
                to, to, to,
                graphics.bufferSource(), poseStack, LightTexture.FULL_BRIGHT,
                false, true);
        poseStack.popPose();
        graphics.flush();
        Lighting.setupFor3DItems();

        poseStack.popPose();
    }
}
