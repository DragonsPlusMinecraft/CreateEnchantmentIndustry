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

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlock;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class BlazeForgerArmInteractionPoint extends ArmInteractionPoint {
    public BlazeForgerArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger))
            return stack;
        ItemStack input = stack.copy();
        InteractionResultHolder<ItemStack> result = BlazeExperienceBlock.applyFuel(cachedState, level, pos, input, false, false, simulate);
        if (result.getResult().consumesAction()) {
            ItemStack remainder = result.getObject();
            if (input.isEmpty()) {
                return remainder;
            } else {
                if (!simulate)
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
                return input;
            }
        } else if (result.getResult() == InteractionResult.PASS) {
            return forger.insertAutomationItem(input, simulate);
        }
        return input;
    }

    @Override
    public ItemStack extract(int slot, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger) {
            return forger.extractAutomationItem(slot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotCount() {
        if (level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger)
            return forger.getAutomationSlotCount();
        return 0;
    }

    public static class Type extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return CEIBlocks.BLAZE_FORGER.has(state);
        }

        @Nullable
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BlazeForgerArmInteractionPoint(this, level, pos, state);
        }
    }
}
