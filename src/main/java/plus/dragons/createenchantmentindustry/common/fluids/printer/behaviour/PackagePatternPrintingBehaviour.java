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
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class PackagePatternPrintingBehaviour implements PrintingBehaviour {
    private final ItemStack pattern;
    private final SmartFluidTankBehaviour tank;

    public PackagePatternPrintingBehaviour(ItemStack pattern, SmartFluidTankBehaviour tank) {
        this.pattern = pattern;
        this.tank = tank;
    }

    public static Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        if (stack.getItem() instanceof PackageItem) {
            String address = stack.get(AllDataComponents.PACKAGE_ADDRESS);
            if (address == null || address.isEmpty())
                return Optional.of(DataResult.success(new PackagePatternPrintingBehaviour(stack.copy(), tank)));
        }
        return Optional.empty();
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        if (stack.getItem() instanceof PackageItem && !stack.is(pattern.getItem()))
            return 1;
        return 0;
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        var amount = fluidStack.getFluidHolder().getData(CEIDataMaps.PRINTING_PATTERN_INGREDIENT);
        return amount == null ? 0 : amount;
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        var result = stack.transmuteCopy(pattern.getItem());
        return result;
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        // Plays SoundEvents.BOOK_PAGE_TURN
        level.levelEvent(1043, pos.below(), 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEILang.translate("gui.goggles.printing.pattern").forGoggles(tooltip);
        var amount = tank.getPrimaryHandler().getFluid().getFluidHolder().getData(CEIDataMaps.PRINTING_PATTERN_INGREDIENT);
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
}
