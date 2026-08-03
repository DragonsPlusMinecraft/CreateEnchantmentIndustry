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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
import static plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer.AffixAugmentorBlockEntity.UNIT_PROCESSING_TIME;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.integration.apotheosis.client.registry.CEIAXPartialModels;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance.LowerBeltCopperCasingAndSupportRenderer;

public class AffixAugmentorRenderer extends LowerBeltCopperCasingAndSupportRenderer<AffixAugmentorBlockEntity> {
    public AffixAugmentorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(AffixAugmentorBlockEntity augmentor, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(augmentor, partialTicks, poseStack, buffer, light, overlay);

        PartialModel plate = augmentor.powered ? CEIAXPartialModels.AFFIX_AUGMENTOR_PLATE_POWERED : CEIAXPartialModels.AFFIX_AUGMENTOR_PLATE;

        BlockState state = augmentor.getBlockState();
        var facing = state.getValue(FACING);

        float unready;
        if (augmentor.powered) {
            unready = augmentor.chargingPercentage == 1 ? 0 : (float) Math.min(1.025 - augmentor.chargingPercentage - partialTicks * 0.025, 1);
        } else {
            unready = augmentor.chargingPercentage == 0 ? 1 : (float) Math.max(1.00 - augmentor.chargingPercentage - (1 - partialTicks) * 0.025, 0);
        }

        float plateOffset = 0;
        if (unready >= 0.8) {
            plateOffset = Mth.lerp((1 - unready) / 0.2f, 0.99f / 16f, 0);
        }
        CachedBuffers.partialFacing(plate, state, facing)
                .light(light)
                .translate(0, plateOffset, 0)
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));

        if (unready <= 0.8) {
            float bColOffset = 0;
            if (unready >= 0.5) {
                bColOffset = Mth.lerp((0.8f - unready) / 0.3f, 2f / 16f, 0);
            }
            CachedBuffers.partialFacing(CEIAXPartialModels.AFFIX_AUGMENTOR_BIG_COLUMN, state, facing)
                    .light(light)
                    .translate(0, bColOffset, 0)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));

            if (unready <= 0.5) {
                float sColOffset = 0;
                if (unready >= 0.35) {
                    sColOffset = Mth.lerp((0.5f - unready) / 0.35f, 2f / 16f, 0);
                }
                CachedBuffers.partialFacing(CEIAXPartialModels.AFFIX_AUGMENTOR_SMALL_COLUMN, state, facing)
                        .light(light)
                        .translate(0, sColOffset, 0)
                        .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));

                if (unready <= 0.35) {
                    float needleOffset = 0;
                    if (unready > 0) {
                        needleOffset = Mth.lerp(1 - unready / 0.35f, 3.5f / 16f, 0);
                    }
                    CachedBuffers.partialFacing(CEIAXPartialModels.AFFIX_AUGMENTOR_NEEDLE, state, facing)
                            .light(light)
                            .center()
                            .scale(1 - unready)
                            .translate(0, needleOffset, 0)
                            .uncenter()
                            .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));

                }
            }
        }
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
