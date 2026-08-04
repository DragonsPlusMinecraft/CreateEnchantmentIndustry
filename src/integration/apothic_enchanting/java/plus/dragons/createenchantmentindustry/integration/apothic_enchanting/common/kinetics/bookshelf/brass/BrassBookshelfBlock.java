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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.brass;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlockEntities;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;

public class BrassBookshelfBlock extends KineticBlock implements IEnchantingBlock, IBE<BrassBookshelfBlockEntity> {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BrassBookshelfBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Class<BrassBookshelfBlockEntity> getBlockEntityClass() {
        return BrassBookshelfBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BrassBookshelfBlockEntity> getBlockEntityType() {
        return CEIABlockEntities.BRASS_BOOKSHELF.get();
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        BrassBookshelfBlockEntity blockEntity = getBlockEntity(level, pos);
        return blockEntity == null ? 0 : blockEntity.eterna() / 2f;
    }

    @Override
    public float getMaxEnchantingPower(BlockState state, LevelReader world, BlockPos pos) {
        return CEIAConfig.server().stats().multipleBrassBookshelfMaxEterna.get();
    }

    @Override
    public float getQuantaBonus(BlockState state, LevelReader world, BlockPos pos) {
        BrassBookshelfBlockEntity blockEntity = getBlockEntity(world, pos);
        return blockEntity == null ? 0 : blockEntity.quanta();
    }

    @Override
    public float getArcanaBonus(BlockState state, LevelReader world, BlockPos pos) {
        BrassBookshelfBlockEntity blockEntity = getBlockEntity(world, pos);
        return blockEntity == null ? 0 : blockEntity.arcana();
    }

    @Override
    public boolean allowsTreasure(BlockState state, LevelReader world, BlockPos pos) {
        BrassBookshelfBlockEntity blockEntity = getBlockEntity(world, pos);
        return blockEntity != null && blockEntity.allowTreasure();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public IRotate.SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }
}
