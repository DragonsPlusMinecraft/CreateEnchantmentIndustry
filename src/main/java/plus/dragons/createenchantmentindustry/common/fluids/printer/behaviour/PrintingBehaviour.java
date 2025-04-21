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
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus.Internal;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;

public interface PrintingBehaviour extends IHaveGoggleInformation {
    @Internal
    List<Provider> PROVIDERS = Util.make(new ArrayList<>(), list -> {
        list.add(AddressPrintingBehaviour::create);
        list.add(PackagePatternPrintingBehaviour::create);
        list.add(CopyPrintingBehaviour::create);
        list.add(CustomNamePrintingBehaviour::create);
        list.add(EnchantedBookPrintingBehaviour::create);
        list.add(WrittenBookPrintingBehaviour::create);
    });

    static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    static DataResult<PrintingBehaviour> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        for (var provider : PROVIDERS) {
            var result = provider.create(level, tank, stack);
            if (result.isPresent())
                return result.get();
        }
        return DataResult.success(new RecipePrintingBehaviour(stack));
    }

    default boolean isValid() {
        return true;
    }

    default boolean isSafeNBT() {
        return true;
    }

    int getRequiredItemCount(Level level, ItemStack stack);

    int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack);

    ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack);

    void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer);

    @FunctionalInterface
    interface Provider {
        Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack);
    }
}
