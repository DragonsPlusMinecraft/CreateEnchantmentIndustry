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

package plus.dragons.createenchantmentindustry.common.kinetics.crusher;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class CrushingWheelExperience {
    private CrushingWheelExperience() {}

    public static void dropForCrushedEntity(CrushingWheelControllerBlockEntity crusher, LivingEntity entity) {
        if (!CEIConfig.kinetics().crushingWheelKillDropXp.get())
            return;
        if (!(entity.level() instanceof ServerLevel serverLevel))
            return;

        int experience = getScaledExperience(serverLevel, entity);
        if (experience <= 0)
            return;
        if (serverLevel.random.nextFloat() >= CEIConfig.kinetics().crushingWheelKillDropXpChance.getF())
            return;

        int nuggets = getExperienceNuggets(serverLevel.random, experience);
        if (nuggets <= 0)
            nuggets = 1;

        Vec3 outputPosition = getOutputPosition(crusher);
        ItemEntity expItem = new ItemEntity(serverLevel, outputPosition.x(), outputPosition.y(), outputPosition.z(), AllItems.EXP_NUGGET.asStack(nuggets));
        expItem.setDeltaMovement(getOutputSpeed(crusher));
        expItem.getPersistentData()
                .put("BypassCrushingWheel", NbtUtils.writeBlockPos(crusher.getBlockPos()));
        serverLevel.addFreshEntity(expItem);
    }

    private static int getScaledExperience(ServerLevel level, LivingEntity entity) {
        int baseExperience = entity.getExperienceReward();
        if (baseExperience <= 0)
            return 0;

        float scaledExperience = baseExperience * CEIConfig.kinetics().crushingWheelKillDropXpScale.getF();
        if (scaledExperience <= 0)
            return 0;

        int experience = Mth.floor(scaledExperience);
        if (level.random.nextFloat() < scaledExperience - experience)
            experience++;
        return Math.max(experience, 1);
    }

    private static int getExperienceNuggets(RandomSource random, int experience) {
        int nuggets = experience / 3;
        if (random.nextFloat() < (experience % 3) / 3f)
            nuggets++;
        return nuggets;
    }

    private static Vec3 getOutputPosition(CrushingWheelControllerBlockEntity crusher) {
        Vec3 centerPos = VecHelper.getCenterOf(crusher.getBlockPos());
        Direction facing = crusher.getBlockState().getValue(CrushingWheelControllerBlock.FACING);
        int offset = facing.getAxisDirection().getStep();
        return centerPos.add(
                facing.getAxis() == Direction.Axis.X ? .55f * offset : 0f,
                facing.getAxis() == Direction.Axis.Y ? .55f * offset : 0f,
                facing.getAxis() == Direction.Axis.Z ? .55f * offset : 0f);
    }

    private static Vec3 getOutputSpeed(CrushingWheelControllerBlockEntity crusher) {
        Direction facing = crusher.getBlockState().getValue(CrushingWheelControllerBlock.FACING);
        int offset = facing.getAxisDirection().getStep();
        return new Vec3(
                (facing.getAxis() == Direction.Axis.X ? 0.25D : 0.0D) * offset,
                offset == 1 ? (facing.getAxis() == Direction.Axis.Y ? 0.5D : 0.0D) : 0.0D,
                (facing.getAxis() == Direction.Axis.Z ? 0.25D : 0.0D) * offset);
    }
}
