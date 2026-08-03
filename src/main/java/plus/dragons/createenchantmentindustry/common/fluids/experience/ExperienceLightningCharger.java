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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.simibubi.create.AllBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class ExperienceLightningCharger {
    private ExperienceLightningCharger() {}

    public static void charge(Level level, BlockPos strikePos) {
        Optional<BlockPos> target = findStrikeTarget(level, strikePos);
        if (target.isEmpty())
            return;
        BlockPos pos = target.get();
        chargeBlock(level, pos, false);

        BlockPos.MutableBlockPos mutable = pos.mutable();
        int branches = level.random.nextInt(3) + 3;
        for (int i = 0; i < branches; i++) {
            int steps = level.random.nextInt(8) + 1;
            randomWalkChargeExperience(level, pos, mutable, steps);
        }
    }

    public static Optional<BlockPos> findStrikeTarget(Level level, BlockPos pos) {
        return findChargeableBlock(level, findLightningRodTarget(level, pos).orElse(pos));
    }

    public static Optional<BlockPos> findLightningRodTarget(Level level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.is(BlazeExperienceBlockEntity.LIGHTNING_ROD_BLOCKS)) {
            if (blockstate.hasProperty(LightningRodBlock.FACING))
                return Optional.of(pos.relative(blockstate.getValue(LightningRodBlock.FACING).getOpposite()));
            return Optional.of(pos.below());
        }
        return Optional.empty();
    }

    public static Optional<BlockPos> findChargeableBlock(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.EXPERIENCE_BLOCK.get()))
            return Optional.of(pos);
        return Optional.empty();
    }

    public static void chargeBlock(Level level, BlockPos pos, boolean particles) {
        level.setBlockAndUpdate(pos, CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState());
        if (particles)
            level.levelEvent(3002, pos, -1);
    }

    private static void randomWalkChargeExperience(Level level, BlockPos pos, BlockPos.MutableBlockPos mutable, int steps) {
        mutable.set(pos);
        for (int i = 0; i < steps; i++) {
            Optional<BlockPos> optional = randomWalkChargeExperience(level, mutable);
            if (optional.isEmpty())
                break;
            mutable.set(optional.get());
        }
    }

    private static Optional<BlockPos> randomWalkChargeExperience(Level level, BlockPos pos) {
        for (BlockPos blockpos : BlockPos.randomInCube(level.random, 10, pos, 1)) {
            Optional<BlockPos> chargeable = findChargeableBlock(level, blockpos);
            if (chargeable.isPresent()) {
                BlockPos target = chargeable.get();
                chargeBlock(level, target, true);
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }
}
