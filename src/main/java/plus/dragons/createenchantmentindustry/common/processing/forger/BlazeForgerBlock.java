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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlock;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;

public class BlazeForgerBlock extends BlazeExperienceBlock<BlazeForgerBlockEntity> {
    public BlazeForgerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(level, pos, placer);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        var blockEntity = getBlockEntity(level, pos);
        if (stack.isEmpty()) {
            if (blockEntity == null)
                return InteractionResult.PASS;
            ItemStack extracted = blockEntity.extractItem(false);
            if (!extracted.isEmpty()) {
                player.getInventory().placeItemBackInInventory(extracted);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }
        var result = super.use(state, level, pos, player, hand, hitResult);
        if (result != InteractionResult.PASS)
            return result;
        if (blockEntity == null)
            return InteractionResult.PASS;
        var remainder = blockEntity.insertItem(stack, false);
        if (ItemStack.isSameItemSameTags(stack, remainder) && remainder.getCount() == stack.getCount())
            return InteractionResult.PASS;
        player.setItemInHand(hand, remainder);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<BlazeForgerBlockEntity> getBlockEntityClass() {
        return BlazeForgerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeForgerBlockEntity> getBlockEntityType() {
        return CEIBlockEntities.BLAZE_FORGER.get();
    }
}
