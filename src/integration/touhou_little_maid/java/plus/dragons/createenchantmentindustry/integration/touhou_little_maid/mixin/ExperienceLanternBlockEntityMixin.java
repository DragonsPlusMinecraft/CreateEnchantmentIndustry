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

package plus.dragons.createenchantmentindustry.integration.touhou_little_maid.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createdragonsplus.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.touhou_little_maid.common.fluids.lantern.TouhouLittleMaidExperienceHelper;

@Mixin(ExperienceLanternBlockEntity.class)
@Restriction(require = @Condition(ModIntegration.Constants.TOUHOU_LITTLE_MAID))
public abstract class ExperienceLanternBlockEntityMixin extends SmartBlockEntity {
    @Shadow(remap = false)
    protected FluidTankBehaviour tank;
    @Shadow(remap = false)
    protected AABB effectiveAABB;

    public ExperienceLanternBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "drainExp", at = @At("TAIL"), remap = false)
    private void create_enchantment_industry$touhouLittleMaid$drainMaidExperience(CallbackInfo ci) {
        if (level == null || level.isClientSide)
            return;
        TouhouLittleMaidExperienceHelper.drainMaidExperience(level, effectiveAABB, tank.getPrimaryHandler());
    }
}
