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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EnchanterBehaviorTemplateItemRenderer {
    public static void renderOnBlockEntity(SmartBlockEntity be, float partialTicks, PoseStack ms,
            MultiBufferSource buffer, int light, int overlay) {
        if (be == null || be.isRemoved())
            return;

        Level level = be.getLevel();
        BlockPos blockPos = be.getBlockPos();

        for (BlockEntityBehaviour b : be.getAllBehaviours()) {
            if (!(b instanceof EnchanterBehaviour behaviour))
                continue;

            if (!behaviour.isActive())
                return;
            if (behaviour.getTemplate().isEmpty())
                return;

            Minecraft mc = Minecraft.getInstance();
            HitResult target = mc.hitResult;
            if (target == null || !(target instanceof BlockHitResult result))
                return;
            Vec3 localHit = target.getLocation().subtract(Vec3.atLowerCornerOf(be.getBlockPos()));
            boolean highlight = behaviour.templateItemTransform.testHit(level, blockPos, be.getBlockState(), localHit);
            behaviour.templateItemTransform.fromSide(result.getDirection());
            if (!highlight) return;

            ValueBoxTransform slotPositioning = behaviour.getTemplateItemSlotPositioning();
            BlockState blockState = be.getBlockState();

            if (!be.isVirtual()) {
                Entity cameraEntity = Minecraft.getInstance().cameraEntity;
                if (cameraEntity != null && level == cameraEntity.level()) {
                    float max = behaviour.getRenderDistance();
                    if (cameraEntity.position()
                            .distanceToSqr(VecHelper.getCenterOf(blockPos)) > (max * max)) {
                        return;
                    }
                }
            }

            if (slotPositioning.shouldRender(level, blockPos, blockState)) {
                ms.pushPose();
                slotPositioning.transform(level, blockPos, blockState, ms);
                ValueBoxRenderer.renderItemIntoValueBox(behaviour.getTemplate(), ms, buffer, light, overlay);
                ms.popPose();
            }
        }
    }
}
