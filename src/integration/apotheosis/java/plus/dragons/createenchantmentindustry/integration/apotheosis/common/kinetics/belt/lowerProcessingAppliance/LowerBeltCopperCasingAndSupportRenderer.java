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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import plus.dragons.createenchantmentindustry.integration.apotheosis.client.registry.CEIAXPartialModels;

public abstract class LowerBeltCopperCasingAndSupportRenderer<T extends SmartBlockEntity> extends SmartBlockEntityRenderer<T> {
    public LowerBeltCopperCasingAndSupportRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, poseStack, buffer, light, overlay);
        var belowState = be.getLevel().getBlockState(be.getBlockPos().below());
        if (AllBlocks.BELT.has(belowState)) {
            if (belowState.getValue(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL && !belowState.getValue(BeltBlock.CASING)) {
                poseStack.pushPose();
                poseStack.translate(0, -1, 0);
                var facing = belowState.getValue(HORIZONTAL_FACING);
                var shift = belowState.getValue(BeltBlock.PART);
                CachedBuffers.partialFacing(shift == BeltPart.MIDDLE ? CEIAXPartialModels.SPECIAL_CASING : CEIAXPartialModels.SPECIAL_CASING_WITH_SHAFT,
                        belowState, facing)
                        .light(light)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));
                poseStack.popPose();
            }
        } else if (AllBlocks.DEPOT.has(belowState)) {
            poseStack.pushPose();
            poseStack.translate(0, -1, 0);
            CachedBuffers.partial(CEIAXPartialModels.SPECIAL_CASING_TOP_ONLY, belowState)
                    .light(light)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));
            poseStack.popPose();
        }
    }
}
