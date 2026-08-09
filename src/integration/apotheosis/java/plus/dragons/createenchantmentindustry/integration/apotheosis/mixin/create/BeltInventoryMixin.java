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

package plus.dragons.createenchantmentindustry.integration.apotheosis.mixin.create;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance.LowerBeltProcessingBehaviour;

@Restriction(require = @Condition(ModIntegration.Constants.APOTHEOSIS))
@Mixin(BeltInventory.class)
public abstract class BeltInventoryMixin {
    @Shadow(remap = false)
    @Final
    BeltBlockEntity belt;

    @WrapMethod(method = "getBeltProcessingAtSegment", remap = false)
    private BeltProcessingBehaviour getBeltProcessingAtSegment$getLowerBeltProcessingBehaviour(int segment, Operation<BeltProcessingBehaviour> original) {
        var bhvr = BlockEntityBehaviour.get(belt.getLevel(), BeltHelper.getPositionForOffset(belt, segment)
                .above(1), LowerBeltProcessingBehaviour.TYPE);
        if (bhvr != null) return bhvr;
        else return original.call(segment);
    }

    @WrapOperation(method = "handleBeltProcessingAndCheckIfRemoved", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/belt/behaviour/BeltProcessingBehaviour;isBlocked(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z", remap = false), remap = false)
    private boolean isBlocked$bypassCheck(BlockGetter world, BlockPos processingSpace, Operation<Boolean> original, @Local(ordinal = 0) BeltProcessingBehaviour bp) {
        if (bp.getType() == LowerBeltProcessingBehaviour.TYPE) return false;
        else return original.call(world, processingSpace);
    }
}
