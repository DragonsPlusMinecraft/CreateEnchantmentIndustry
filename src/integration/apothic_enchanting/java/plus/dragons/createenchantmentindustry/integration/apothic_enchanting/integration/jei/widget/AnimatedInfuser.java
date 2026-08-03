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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.integration.jei.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.registry.CEIAPartialModels;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserRenderer;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusionStats;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks;

public class AnimatedInfuser extends AnimatedKinetics {
    private static final PartialModel[] NEEDLES = {
            CEIAPartialModels.INFUSER_ARCANA_NEEDLE,
            CEIAPartialModels.INFUSER_ETERNA_NEEDLE,
            CEIAPartialModels.INFUSER_QUANTA_NEEDLE,
    };
    private static final float STANDARD_LENGTH = 3 / 16f;

    private FluidStack fluid = FluidStack.EMPTY;
    private InfusionStats stats = InfusionStats.EMPTY;

    public AnimatedInfuser with(FluidStack fluid, InfusionStats stats) {
        this.fluid = fluid;
        this.stats = stats;
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

        blockElement(CEIABlocks.INFUSER.getDefaultState())
                .scale(scale)
                .render(graphics);

        float cycle = (AnimationTickHolder.getRenderTime() - offset * 8) % 30;
        float progress = cycle < 20 ? -InfuserRenderer.getProgress(cycle / 20f * 50f) : 0;
        progress *= scale;

        var eterna = stats.eterna();
        var arcana = stats.arcana();
        var quanta = stats.quanta();

        if (progress == 1) {
            poseStack.pushPose();
            poseStack.translate(0, -1 / 16f * progress - STANDARD_LENGTH, 0);
            for (PartialModel needle : NEEDLES) {
                blockElement(needle)
                        .scale(scale)
                        .render(graphics);
            }
            poseStack.popPose();
        }

        else {
            poseStack.pushPose();
            poseStack.translate(0, -1 / 4f * progress + eterna / 100f * STANDARD_LENGTH * (progress - 1), 0);
            blockElement(CEIAPartialModels.INFUSER_ETERNA_NEEDLE)
                    .scale(scale)
                    .render(graphics);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0, -1 / 4f * progress + quanta / 100f * STANDARD_LENGTH * (progress - 1), 0);
            blockElement(CEIAPartialModels.INFUSER_QUANTA_NEEDLE)
                    .scale(scale)
                    .render(graphics);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0, -1 / 4f * progress + arcana / 100f * STANDARD_LENGTH * (progress - 1), 0);
            blockElement(CEIAPartialModels.INFUSER_ARCANA_NEEDLE)
                    .scale(scale)
                    .render(graphics);
            poseStack.popPose();
        }

        blockElement(AllBlocks.BASIN.getDefaultState())
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
