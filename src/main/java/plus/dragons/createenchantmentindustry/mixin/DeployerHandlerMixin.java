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

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import plus.dragons.createenchantmentindustry.common.kinetics.deployer.DeployerExtension;

@Mixin(value = DeployerHandler.class, remap = false)
public class DeployerHandlerMixin {
    @Unique
    private static final ThreadLocal<Integer> CEI_DROPPED_EXPERIENCE = ThreadLocal.withInitial(() -> 0);

    @Redirect(method = "tryHarvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onBlockBreakEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/GameType;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;)I", remap = false), remap = false)
    private static int tryHarvestBlock$captureExperience(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        int experience = ForgeHooks.onBlockBreakEvent(level, gameType, player, pos);
        if (experience >= 0 && player instanceof DeployerFakePlayer deployer)
            experience = DeployerExtension.handleBlockExperience(deployer, experience);
        CEI_DROPPED_EXPERIENCE.set(Math.max(0, experience));
        return experience;
    }

    @Redirect(method = "tryHarvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V", remap = true), remap = false)
    private static void tryHarvestBlock$applyExperience(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience, @Local(argsOnly = true) ServerPlayer player) {
        if (!(player instanceof DeployerFakePlayer)) {
            state.spawnAfterBreak(level, pos, stack, dropExperience);
            return;
        }
        state.spawnAfterBreak(level, pos, stack, false);
        int experience = CEI_DROPPED_EXPERIENCE.get();
        CEI_DROPPED_EXPERIENCE.remove();
        if (experience > 0)
            state.getBlock().popExperience(level, pos, experience);
    }
}
