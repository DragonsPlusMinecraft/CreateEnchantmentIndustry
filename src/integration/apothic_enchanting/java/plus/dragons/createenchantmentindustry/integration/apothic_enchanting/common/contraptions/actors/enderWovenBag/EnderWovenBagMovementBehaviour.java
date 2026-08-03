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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import plus.dragons.createenchantmentindustry.common.network.CEINetwork;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;

public class EnderWovenBagMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide) return;

        var entities = getEntities(context);
        var realPosition = getRealPosition(context);

        if (context.data.contains("AnchorPos")) {
            BlockPos pos = NbtUtils.readBlockPos(context.data.getCompound("AnchorPos"));
            Vec3 target = VecHelper.getCenterOf(pos);

            if (!context.stall && context.position.closerThan(target, target.distanceTo(context.position.add(context.motion)))) {
                context.stall = true;
                context.data.remove("AnchorPos");
                var contraption = context.contraption.entity;
                CEINetwork.CHANNEL.send(
                        PacketDistributor.TRACKING_CHUNK.with(
                                () -> ((ServerLevel) context.world).getChunkAt(context.contraption.entity.blockPosition())),
                        new ContraptionEnderWovenBagPocketChangePacket(contraption.getId(), context.localPos, true));
                return;
            }
        }

        if (context.stall) {
            if (entities.count() == 0) {
                cancelStall(context);
                context.data.putInt("Disabled", CEIAConfig.server().utility().enderWovenBagStopDisableDurationAfterReleasingOnContraption.get());
                return;
            }
            if (context.world.getGameTime() % 5 == 0) {
                var entity = entities.pop(context.world);
                assert entity != null;
                entity.setPos(getReleasePosition(context));
                context.world.addFreshEntity(entity);
                setEntities(context, entities);
            }
            return;
        }

        if (context.data.contains("Disabled")) {
            if (context.data.getInt("Disabled") == 1) context.data.remove("Disabled");
            else context.data.putInt("Disabled", context.data.getInt("Disabled") - 1);
            return;
        }

        if (context.world.getGameTime() % 5 == 0) {
            var effectiveAABB = new AABB(realPosition.subtract(0.5d, 0.5d, 0.5d), realPosition.add(0.5d, 0.5d, 0.5d)).inflate(1);
            if (!entities.full()) {
                var targets = context.world.getEntitiesOfClass(LivingEntity.class, effectiveAABB, CaptureEntityBehaviour::test);
                if (!targets.isEmpty()) {
                    for (var entity : targets) {
                        entities.push(entity);
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        if (entities.full()) {
                            var contraption = context.contraption.entity;
                            CEINetwork.CHANNEL.send(
                                    PacketDistributor.TRACKING_CHUNK.with(
                                            () -> ((ServerLevel) context.world).getChunkAt(context.contraption.entity.blockPosition())),
                                    new ContraptionEnderWovenBagPocketChangePacket(contraption.getId(), context.localPos, false));
                            break;
                        }

                    }
                    setEntities(context, entities);
                }

                if (!entities.full() && CEIAConfig.server().utility().enderWovenBagPullToggle.get()) {
                    targets = context.world.getEntitiesOfClass(LivingEntity.class,
                            effectiveAABB.inflate(CEIAConfig.server().utility().enderWovenBagPullRadius.get()), CaptureEntityBehaviour::test);
                    for (var entity : targets) {
                        var pushForce = CEIAConfig.server().utility().enderWovenBagPullForceMultiplier.get() * 1 / entity.position().distanceTo(realPosition);
                        var direction = realPosition.subtract(entity.position()).normalize().multiply(pushForce, pushForce, pushForce);
                        entity.push(direction.x, direction.y, direction.z);
                    }
                }
            }
        }
    }

    @Override
    public Vec3 getActiveAreaOffset(MovementContext context) {
        return Vec3.atLowerCornerOf(context.state.getValue(EnderWovenBagBlock.FACING)
                .getNormal())
                .scale(1.85f);
    }

    private Vec3 getRealPosition(MovementContext context) {
        return context.position.subtract(getActiveAreaOffset(context));
    }

    private Vec3 getReleasePosition(MovementContext context) {
        return context.position.subtract(getActiveAreaOffset(context).scale(0.85 / 1.85));
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
            ContraptionMatrices matrices, MultiBufferSource buffer) {
        EnderWovenBagRenderer.renderInContraption(context, renderWorld, matrices, buffer);
    }

    @Override
    public void cancelStall(MovementContext context) {
        reset(context);
    }

    public void reset(MovementContext context) {
        context.data.remove("AnchorPos");
        context.data.remove("Disabled");
        context.stall = false;
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        BlockState state = context.world.getBlockState(pos);
        if (state.is(Blocks.REDSTONE_BLOCK) && getEntities(context).count() > 0) {
            context.data.put("AnchorPos", NbtUtils.writeBlockPos(pos));
        }
    }

    @Override
    public void writeExtraData(MovementContext context) {
        context.blockEntityData.put("Entities", getEntities(context).tag());
    }

    private static StoredEntities getEntities(MovementContext context) {
        if (!(context.temporaryData instanceof StoredEntities)) {
            context.temporaryData = StoredEntities.parse(context.blockEntityData.get("Entities"));
        }
        return (StoredEntities) context.temporaryData;
    }

    private void setEntities(MovementContext context, StoredEntities entities) {
        context.temporaryData = entities;
    }

    @OnlyIn(Dist.CLIENT)
    static boolean renderFull(MovementContext context) {
        return context.data.contains("RenderFull");
    }
}
