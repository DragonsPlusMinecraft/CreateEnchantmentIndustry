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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class MechanicalGrindStoneItem extends BlockItem {
    public MechanicalGrindStoneItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ManualApplicationRecipe createRecipe() {
        return new ProcessingRecipeBuilder<>(ManualApplicationRecipe::new, CEIBlocks.GRINDSTONE_DRAIN.getId())
                .require(AllBlocks.ITEM_DRAIN)
                .require(CEIBlocks.MECHANICAL_GRINDSTONE)
                .output(CEIBlocks.GRINDSTONE_DRAIN)
                .build();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.place(new PlaceContext(context));
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        if (context instanceof PlaceContext placeContext)
            return placeContext.getPlacementState();
        return super.getPlacementState(context);
    }

    public class PlaceContext extends BlockPlaceContext {
        private final boolean clickedDrain;

        public PlaceContext(UseOnContext context) {
            super(context);
            var clickedPos = context.getClickedPos();
            var clickedState = context.getLevel().getBlockState(clickedPos);
            this.clickedDrain = clickedState.is(AllBlocks.ITEM_DRAIN.get());
            this.replaceClicked |= this.clickedDrain;
        }

        @Nullable
        public BlockState getPlacementState() {
            if (clickedDrain) {
                var facing = getHorizontalDirection().getOpposite();
                return CEIBlocks.GRINDSTONE_DRAIN.getDefaultState()
                        .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, facing);
            }
            return MechanicalGrindStoneItem.super.getPlacementState(this);
        }
    }
}
