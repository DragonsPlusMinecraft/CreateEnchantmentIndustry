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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.creative;

import com.simibubi.create.foundation.block.IBE;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlockEntities;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;

public class CreativeBookshelfBlock extends HorizontalDirectionalBlock implements IEnchantingBlock, IBE<CreativeBookshelfBlockEntity> {
    public CreativeBookshelfBlock(Properties props) {
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
                .setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(1, 1, 1, 15, 15, 15);
    }

    @Override
    public Class<CreativeBookshelfBlockEntity> getBlockEntityClass() {
        return CreativeBookshelfBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeBookshelfBlockEntity> getBlockEntityType() {
        return CEIABlockEntities.CREATIVE_BOOKSHELF.get();
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        CreativeBookshelfBlockEntity blockEntity = getBlockEntity(level, pos);
        return blockEntity == null ? 0 : blockEntity.eterna() / 2f;
    }

    @Override
    public float getMaxEnchantingPower(BlockState state, LevelReader world, BlockPos pos) {
        return 100;
    }

    @Override
    public float getQuantaBonus(BlockState state, LevelReader world, BlockPos pos) {
        CreativeBookshelfBlockEntity blockEntity = getBlockEntity(world, pos);
        return blockEntity == null ? 0 : blockEntity.quanta();
    }

    @Override
    public float getArcanaBonus(BlockState state, LevelReader world, BlockPos pos) {
        CreativeBookshelfBlockEntity blockEntity = getBlockEntity(world, pos);
        return blockEntity == null ? 0 : blockEntity.arcana();
    }

    @Override
    public boolean allowsTreasure(BlockState state, LevelReader world, BlockPos pos) {
        return CEIAConfig.server().stats().creativeBookshelfAllowTreasures.get();
    }
}
