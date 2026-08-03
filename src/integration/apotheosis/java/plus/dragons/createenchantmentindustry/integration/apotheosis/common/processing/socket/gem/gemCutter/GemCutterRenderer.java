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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
import static plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutterBlockEntity.UNIT_PROCESSING_TIME;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.integration.apotheosis.client.registry.CEIAXPartialModels;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance.LowerBeltCopperCasingAndSupportRenderer;

public class GemCutterRenderer extends LowerBeltCopperCasingAndSupportRenderer<GemCutterBlockEntity> {
    public GemCutterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GemCutterBlockEntity cutter, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(cutter, partialTicks, poseStack, buffer, light, overlay);

        PartialModel needle = cutter.powered ? CEIAXPartialModels.GEM_CUTTER_CRYSTAL_NEEDLE_POWERED : CEIAXPartialModels.GEM_CUTTER_CRYSTAL_NEEDLE;
        PartialModel vertAli = cutter.powered ? CEIAXPartialModels.GEM_CUTTER_CRYSTAL_SPHERE_VERTICAL_ALIGNED_FRAME_POWERED : CEIAXPartialModels.GEM_CUTTER_CRYSTAL_SPHERE_VERTICAL_ALIGNED_FRAME;
        PartialModel vert = cutter.powered ? CEIAXPartialModels.GEM_CUTTER_CRYSTAL_SPHERE_VERTICAL_FRAME_POWERED : CEIAXPartialModels.GEM_CUTTER_CRYSTAL_SPHERE_VERTICAL_FRAME;
        PartialModel hori = cutter.powered ? CEIAXPartialModels.GEM_CUTTER_CRYSTAL_SPHERE_HORIZONTAL_FRAME_POWERED : CEIAXPartialModels.GEM_CUTTER_CRYSTAL_SPHERE_HORIZONTAL_FRAME;

        BlockState state = cutter.getBlockState();
        var facing = state.getValue(FACING);

        CachedBuffers.partialFacing(needle, state, facing)
                .light(light)
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));

        float unready;
        if (cutter.powered) {
            unready = cutter.chargingPercentage == 1 ? 0 : (float) Math.min(1.025 - cutter.chargingPercentage - partialTicks * 0.025, 1);
        } else {
            unready = cutter.chargingPercentage == 0 ? 1 : (float) Math.max(1.00 - cutter.chargingPercentage - (1 - partialTicks) * 0.025, 0);
        }

        float radIniVertAli = (float) (1 * Math.PI * unready);
        float radIniVert = (float) (1.5 * Math.PI * unready);
        float radIniHori = (float) (0.5 * Math.PI * unready);

        float progress = (float) getProgress((200 - cutter.processingTicks) + partialTicks);
        float radProg = (float) (2 * Math.PI * progress);

        CachedBuffers.partialFacing(vertAli, state, facing)
                .light(light)
                .center()
                .rotate(facing.getClockWise().getAxis(), radIniVertAli - radProg)
                .uncenter()
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));
        CachedBuffers.partialFacing(vert, state, facing)
                .light(light)
                .center()
                .scale(1 - 0.05f * unready)
                .rotate(facing.getAxis(), radIniVertAli - radProg)
                .rotate(Direction.Axis.Y, radIniVert + radProg)
                .uncenter()
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));
        CachedBuffers.partialFacing(hori, state, facing)
                .light(light)
                .center()
                .scale(1 - 0.1f * unready)
                .rotate(facing.getClockWise().getAxis(), radIniVertAli - radProg)
                .rotate(Direction.Axis.Y, radIniVert + radProg)
                .rotate(facing.getAxis(), radIniHori - radProg)
                .uncenter()
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));
    }

    // -1 to 1 = 2PI to -2PI
    private static double getProgress(float partialTicks) {
        if (partialTicks < 1) // Not 0. Prevent shaking
            return 0;
        else if (partialTicks < 40) {
            if (partialTicks < 20) return Mth.lerp(partialTicks / 20, 0, 0.4);
            else return Mth.lerp((40 - partialTicks) / 20, 0, 0.4);
        } else if (partialTicks < UNIT_PROCESSING_TIME - 40) {
            return Mth.lerp((14400 - (25600 - partialTicks * partialTicks) / 14400), 0, 4);
        } else if (partialTicks <= UNIT_PROCESSING_TIME) {
            if (partialTicks < UNIT_PROCESSING_TIME - 20) return Mth.lerp((partialTicks - 160) / 20, 0, -0.4);
            else return Mth.lerp((200 - partialTicks) / 20, 0, -0.4);
        }
        return 0;
    }
}
