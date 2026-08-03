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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.mixin.create;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import java.util.Optional;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlockEntity;

@Restriction(require = @Condition(ModIntegration.Constants.APOTHIC_ENCHANTING))
@Mixin(BasinBlockEntity.class)
public abstract class BasinBlockEntityMixin extends SmartBlockEntity {
    public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapMethod(method = "onEmptied", remap = false)
    public void onEmptied$checkInfuser(Operation<Void> original) {
        getInfuser().ifPresent(be -> be.basinRemoved = true);
        original.call();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;getOperator()Ljava/util/Optional;", remap = false), remap = false)
    private void tick$checkInfuser(CallbackInfo ci) {
        getInfuser().ifPresent(be -> be.basinChecker.scheduleUpdate());
    }

    @Unique
    private Optional<InfuserBlockEntity> getInfuser() {
        if (level == null)
            return Optional.empty();
        BlockEntity be = level.getBlockEntity(worldPosition.above(2));
        if (be instanceof InfuserBlockEntity)
            return Optional.of((InfuserBlockEntity) be);
        return Optional.empty();
    }
}
