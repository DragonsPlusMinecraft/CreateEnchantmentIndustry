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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import com.mojang.serialization.DataResult;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class CopyPrintingBehaviour implements PrintingBehaviour {
    private final SupportsItemCopying itemCopying;
    private final ItemStack original;
    private final SmartFluidTankBehaviour tank;

    public CopyPrintingBehaviour(SupportsItemCopying itemCopying, ItemStack original, SmartFluidTankBehaviour tank) {
        this.itemCopying = itemCopying;
        this.original = original;
        this.tank = tank;
    }

    public static Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        if (stack.getItem() instanceof SupportsItemCopying copiable)
            return Optional.of(copiable.canCopyFromItem(stack)
                    ? DataResult.success(new CopyPrintingBehaviour(copiable, stack, tank))
                    : DataResult.error(() -> CEICommon.asLocalization("gui.printer.copy.invalid")));
        else if (stack.is(AllItems.SCHEMATIC.get()))
            return Optional.of(DataResult.success(new CopyPrintingBehaviour(SchematicItemCopying.INSTANCE, stack, tank)));
        return Optional.empty();
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        if (itemCopying == SchematicItemCopying.INSTANCE) {
            if (stack.is(AllItems.EMPTY_SCHEMATIC.get())) return 1;
            else return 0;
        }
        if (ItemStack.isSameItem(original, stack) && itemCopying.canCopyToItem(stack))
            return 1;
        return 0;
    }

    @Override
    public boolean isSafeNBT() {
        return false;
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        var amount = CEIDataMaps.PRINTING_COPY_INGREDIENT.get(fluidStack.getFluid());
        return amount == null ? 0 : amount;
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        return itemCopying.createCopy(original, 1);
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        // Plays SoundEvents.BOOK_PAGE_TURN
        level.levelEvent(1043, pos.below(), 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEILang.translate("gui.goggles.printing.copy").forGoggles(tooltip);
        CEILang.item(original).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        var amount = CEIDataMaps.PRINTING_COPY_INGREDIENT.get(tank.getPrimaryHandler().getFluid().getFluid());
        if (amount != null)
            CEILang.translate("gui.goggles.printing.cost",
                    CEILang.number(amount)
                            .add(CreateLang.translate("generic.unit.millibuckets"))
                            .style(amount <= CEIConfig.fluids().printerFluidCapacity.get()
                                    ? ChatFormatting.GREEN
                                    : ChatFormatting.RED))
                    .forGoggles(tooltip, 1);
        else if (!tank.getPrimaryHandler().getFluid().isEmpty()) {
            CEILang.translate("gui.goggles.printing.incorrect_liquid").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        return true;
    }

    private static class SchematicItemCopying implements SupportsItemCopying {
        static SupportsItemCopying INSTANCE = new SchematicItemCopying();

        @Override
        public ItemStack createCopy(ItemStack original, int count) {
            return original.copyWithCount(count);
        }

        @Override
        public boolean canCopyFromItem(ItemStack item) {
            throw new UnsupportedOperationException("this method should not be called!");
        }

        @Override
        public boolean canCopyToItem(ItemStack item) {
            throw new UnsupportedOperationException("this method should not be called!");
        }
    }
}
