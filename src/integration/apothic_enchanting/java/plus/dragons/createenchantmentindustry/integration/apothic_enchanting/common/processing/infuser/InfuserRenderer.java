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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser;

import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlockEntity.PROCESSING_TIME;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.registry.CEIAPartialModels;

public class InfuserRenderer extends SmartBlockEntityRenderer<InfuserBlockEntity> {
    private static final PartialModel[] NEEDLES = {
            CEIAPartialModels.INFUSER_ARCANA_NEEDLE,
            CEIAPartialModels.INFUSER_ETERNA_NEEDLE,
            CEIAPartialModels.INFUSER_QUANTA_NEEDLE,
    };
    private static final float STANDARD_LENGTH = 3 / 16f;

    public InfuserRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(InfuserBlockEntity infuser, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(infuser, partialTicks, poseStack, buffer, light, overlay);
        SmartFluidTankBehaviour.TankSegment tank = infuser.tank.getPrimaryTank();
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

        BlockState state = infuser.getBlockState();

        var eterna = infuser.infusionStats.eterna();
        var arcana = infuser.infusionStats.arcana();
        var quanta = infuser.infusionStats.quanta();

        if (infuser.running) {
            float progress = getProgress(infuser.processingTicks - partialTicks);

            if (progress == 1) {
                poseStack.pushPose();
                poseStack.translate(0, -1 / 16f * progress - STANDARD_LENGTH, 0);
                for (PartialModel needle : NEEDLES) {
                    CachedBuffers.partial(needle, state)
                            .light(light)
                            .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                }
                poseStack.popPose();
            }

            else {
                poseStack.pushPose();
                poseStack.translate(0, -1 / 4f * progress + eterna / 100f * STANDARD_LENGTH * (progress - 1), 0);
                CachedBuffers.partial(CEIAPartialModels.INFUSER_ETERNA_NEEDLE, state)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0, -1 / 4f * progress + quanta / 100f * STANDARD_LENGTH * (progress - 1), 0);
                CachedBuffers.partial(CEIAPartialModels.INFUSER_QUANTA_NEEDLE, state)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0, -1 / 4f * progress + arcana / 100f * STANDARD_LENGTH * (progress - 1), 0);
                CachedBuffers.partial(CEIAPartialModels.INFUSER_ARCANA_NEEDLE, state)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                poseStack.popPose();
            }
        }

        else {
            poseStack.pushPose();
            poseStack.translate(0, -eterna / 100f * STANDARD_LENGTH - 0.001, 0);
            CachedBuffers.partial(CEIAPartialModels.INFUSER_ETERNA_NEEDLE, state)
                    .light(light)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
            poseStack.popPose();

            if (arcana < 20) {
                poseStack.pushPose();
                poseStack.translate(0, -arcana / 100f * STANDARD_LENGTH - 0.001, 0);
                CachedBuffers.partial(CEIAPartialModels.INFUSER_ARCANA_NEEDLE, state)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                poseStack.popPose();
            } else {
                double distortion = 0.03f * arcana / 100f * ((infuser.getLevel().getGameTime() % 40 - partialTicks) / 40f + 0.001);
                poseStack.pushPose();
                poseStack.translate(-distortion, -arcana / 100f * STANDARD_LENGTH - 0.001, -distortion);
                CachedBuffers.partial(CEIAPartialModels.INFUSER_ARCANA_NEEDLE, state)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                poseStack.translate(distortion * 2, 0, distortion * 2);
                CachedBuffers.partial(CEIAPartialModels.INFUSER_ARCANA_NEEDLE, state)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
                poseStack.popPose();
            }

            poseStack.pushPose();
            poseStack.translate(0, -quanta / 100f * STANDARD_LENGTH +
                    (quanta < 20 ? 0 : Math.random() * 0.05f * STANDARD_LENGTH) - 0.001, 0);
            CachedBuffers.partial(CEIAPartialModels.INFUSER_QUANTA_NEEDLE, state)
                    .light(light)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
            poseStack.popPose();
        }
    }

    public static float getProgress(float partialTicks) {
        if (partialTicks < 0) {
            return 0;
        } else if (partialTicks < 10) {
            return Mth.lerp(partialTicks / 10, 0, 1);
        } else if (partialTicks < PROCESSING_TIME - 10) {
            return 1;
        } else if (partialTicks <= PROCESSING_TIME) {
            return Mth.lerp((PROCESSING_TIME - partialTicks) / 10, 0, 1);
        }
        return 0;
    }
}
