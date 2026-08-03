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

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;

public class CaptureEntityBehaviour extends BlockEntityBehaviour implements IHaveGoggleInformation {
    public static final BehaviourType<CaptureEntityBehaviour> TYPE = new BehaviourType<>("capture_entity");
    private static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation("forge", "capturing_not_supported"));
    protected AABB effectiveAABB;
    protected StoredEntities entities;
    protected boolean release;

    public CaptureEntityBehaviour(SmartBlockEntity be) {
        super(be);
        effectiveAABB = new AABB(be.getBlockPos()).inflate(1);
        entities = new StoredEntities();
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.put("Entities", entities.tag());
        nbt.putBoolean("Release", release);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        this.entities = StoredEntities.parse(nbt.get("Entities"));
        this.release = nbt.getBoolean("Release");
    }

    public Map<Component, Integer> getEntityNames(Level level) {
        return entities.getEntityNames(level);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void lazyTick() {
        if (blockEntity.getLevel().isClientSide || blockEntity.isVirtual()) return; // TODO Particle FX

        if (release) {
            if (entities.count() == 0) return;
            Direction facing = blockEntity.getBlockState().getValue(EnderWovenBagBlock.FACING);
            var entity = entities.pop(blockEntity.getLevel());
            assert entity != null;
            entity.setPos(Vec3.atBottomCenterOf(blockEntity.getBlockPos().relative(facing)));
            blockEntity.getLevel().addFreshEntity(entity);
            blockEntity.sendData();
            blockEntity.setChanged();
            return;
        }

        if (!full()) {
            var targets = blockEntity.getLevel().getEntitiesOfClass(LivingEntity.class, effectiveAABB, CaptureEntityBehaviour::test);
            if (!targets.isEmpty()) {
                for (var entity : targets) {
                    entities.push(entity);
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    if (full()) break;
                }
                blockEntity.sendData();
                blockEntity.setChanged();
            }

            if (!full() && CEIAConfig.server().utility().enderWovenBagPullToggle.get()) {
                targets = blockEntity.getLevel().getEntitiesOfClass(LivingEntity.class,
                        effectiveAABB.inflate(CEIAConfig.server().utility().enderWovenBagPullRadius.get()), CaptureEntityBehaviour::test);
                for (var entity : targets) {
                    var pushForce = CEIAConfig.server().utility().enderWovenBagPullForceMultiplier.get() * 1 / entity.position().distanceTo(blockEntity.getBlockPos().getCenter());
                    var direction = blockEntity.getBlockPos().getCenter().subtract(entity.position()).normalize().multiply(pushForce, pushForce, pushForce);
                    entity.push(direction.x, direction.y, direction.z);
                }
            }
        }
    }

    public boolean full() {
        return entities.full();
    }

    static boolean test(LivingEntity target) {
        return target.isAlive() && target instanceof Mob
                && (!target.getType().is(Tags.EntityTypes.BOSSES) || CEIAConfig.server().utility().enderWovenBagPullBossToggle.get())
                && !target.getType().is(CAPTURING_NOT_SUPPORTED)
                && !target.isRemoved() && !target.isPassenger() && target.getType().canSerialize();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (entities.count() == 0) {
            CEIALang.translate("gui.goggles.no_captured_entity").style(ChatFormatting.GRAY).forGoggles(tooltip);
        } else
            getEntityNames(blockEntity.getLevel()).forEach((key, value) -> {
                var builder = CEIALang.builder().add(key.copy());
                if (value > 1)
                    builder.add(CEIALang.text(" x" + value).style(ChatFormatting.GRAY).component());
                builder.forGoggles(tooltip, 1);
            });
        return true;
    }
}
