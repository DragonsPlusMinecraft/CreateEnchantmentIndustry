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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;

class SuperFuelFluidHandler implements IFluidHandler {
    private static final int NORMAL_TANK = 0;
    private static final int SUPER_TANK = 1;
    private final Supplier<SmartFluidTank> normalTank;
    private final Supplier<SmartFluidTank> superTank;
    private final BooleanSupplier canFillSuperTank;

    SuperFuelFluidHandler(Supplier<SmartFluidTank> normalTank, Supplier<SmartFluidTank> superTank, BooleanSupplier canFillSuperTank) {
        this.normalTank = normalTank;
        this.superTank = superTank;
        this.canFillSuperTank = canFillSuperTank;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getTank(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getTank(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getFluid() == CEIAXFluids.APOTHEOTIC_ESSENCE.get();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(NORMAL_TANK, resource))
            return 0;
        int filled = normalTank.get().fill(resource, action);
        int remaining = resource.getAmount() - filled;
        if (remaining <= 0 || !canFillSuperTank.getAsBoolean())
            return filled;
        FluidStack remainder = resource.copy();
        remainder.setAmount(remaining);
        return filled + superTank.get().fill(remainder, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.getFluid() != CEIAXFluids.APOTHEOTIC_ESSENCE.get())
            return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0)
            return FluidStack.EMPTY;
        int drained = superTank.get().drain(maxDrain, action).getAmount();
        int remaining = maxDrain - drained;
        if (remaining > 0)
            drained += normalTank.get().drain(remaining, action).getAmount();
        return drained <= 0 ? FluidStack.EMPTY : new FluidStack(CEIAXFluids.APOTHEOTIC_ESSENCE.get(), drained);
    }

    private SmartFluidTank getTank(int tank) {
        return switch (tank) {
            case NORMAL_TANK -> normalTank.get();
            case SUPER_TANK -> superTank.get();
            default -> throw new IllegalArgumentException("Tank " + tank + " is not in range [0, 2)");
        };
    }
}
