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

package plus.dragons.createenchantmentindustry.mixin;

import com.simibubi.create.AllBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends Entity {
    @Shadow
    protected abstract BlockPos getStrikePosition();

    private LightningBoltMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;clearCopperOnLightningStrike(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void tick$chargeExperienceOnLightingStrike(CallbackInfo ci) {
        if (!this.getPersistentData().getBoolean(BlazeExperienceBlockEntity.LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY))
            if (this.random.nextFloat() > CEIConfig.processing().regularLightningStrikeTransformXpBlockChance.get())
                return;
        Level level = this.level();
        BlockPos pos = this.getStrikePosition();
        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.is(BlazeExperienceBlockEntity.LIGHTNING_ROD_BLOCKS)) {
            pos = pos.relative(blockstate.getValue(LightningRodBlock.FACING).getOpposite());
            blockstate = level.getBlockState(pos);
        }

        if (blockstate.is(AllBlocks.EXPERIENCE_BLOCK)) {
            level.setBlockAndUpdate(pos, CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState());
            BlockPos.MutableBlockPos mutable = pos.mutable();
            int i = level.random.nextInt(3) + 3;

            for (int j = 0; j < i; j++) {
                int k = level.random.nextInt(8) + 1;
                tick$randomWalkChargeExperience(level, pos, mutable, k);
            }
        }
    }

    @Unique
    private static void tick$randomWalkChargeExperience(Level level, BlockPos pos, BlockPos.MutableBlockPos mutable, int steps) {
        mutable.set(pos);
        for (int i = 0; i < steps; i++) {
            Optional<BlockPos> optional = tick$randomWalkChargeExperience(level, mutable);
            if (optional.isEmpty()) {
                break;
            }
            mutable.set(optional.get());
        }
    }

    @Unique
    private static Optional<BlockPos> tick$randomWalkChargeExperience(Level level, BlockPos pos) {
        for (BlockPos blockpos : BlockPos.randomInCube(level.random, 10, pos, 1)) {
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(AllBlocks.EXPERIENCE_BLOCK)) {
                level.setBlockAndUpdate(blockpos, CEIBlocks.SUPER_EXPERIENCE_BLOCK.getDefaultState());
                level.levelEvent(3002, blockpos, -1);
                return Optional.of(blockpos);
            }
        }
        return Optional.empty();
    }
}
