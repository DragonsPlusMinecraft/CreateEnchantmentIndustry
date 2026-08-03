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

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;

public class ExperienceHatchBlock extends HorizontalDirectionalBlock
        implements IBE<ExperienceHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public ExperienceHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        if (context.getClickedFace().getAxis().isVertical())
            return null;
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getItemInHand(hand).isEmpty())
            return InteractionResult.PASS;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (player instanceof FakePlayer)
            return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos.relative(state.getValue(FACING)));
        if (blockEntity == null)
            return InteractionResult.PASS;

        IFluidHandler tankCapability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
        if (tankCapability == null)
            return InteractionResult.PASS;

        ExperienceHatchBehaviour filter = BlockEntityBehaviour.get(level, pos, ExperienceHatchBehaviour.TYPE);
        if (filter == null)
            return InteractionResult.PASS;

        if (player.isSecondaryUseActive()) {
            FluidStack fluid = filter.getFluidToDrain();
            fluid = tankCapability.drain(fluid, FluidAction.EXECUTE);
            if (fluid.isEmpty())
                return InteractionResult.PASS;
            blockEntity.setChanged();
            if (level instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
            int experience = ExperienceHelper.getExperienceFromFluid(fluid);
            player.giveExperiencePoints(experience);
            CEIAdvancements.SPIRITUAL_RETURN.awardTo(player);
            return InteractionResult.SUCCESS;
        } else {
            int experience = ExperienceHelper.getExperienceForPlayer(player);
            FluidStack fluid = filter.getFluidToFill(experience);
            int filled = tankCapability.fill(fluid, FluidAction.EXECUTE);
            if (filled == 0)
                return InteractionResult.PASS;
            blockEntity.setChanged();
            if (level instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
            FluidStack insertedFluid = fluid.copy();
            insertedFluid.setAmount(filled);
            experience = ExperienceHelper.getExperienceFromFluid(insertedFluid);
            player.giveExperiencePoints(-experience);
            CEIAdvancements.SPIRIT_TAKING.awardTo(player);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<ExperienceHatchBlockEntity> getBlockEntityClass() {
        return ExperienceHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExperienceHatchBlockEntity> getBlockEntityType() {
        return CEIBlockEntities.EXPERIENCE_HATCH.get();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathComputationType) {
        return false;
    }
}
