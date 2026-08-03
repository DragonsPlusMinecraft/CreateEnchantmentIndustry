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

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class ExperienceHatchBehaviour extends FilteringBehaviour {
    public static final BehaviourType<ExperienceHatchBehaviour> TYPE = new BehaviourType<>();
    public static final int POINTS_PER_SCROLL = 10;

    public ExperienceHatchBehaviour(SmartBlockEntity blockEntity, ValueBoxTransform slot) {
        super(blockEntity, slot);
        forFluids();
        count = 0;
    }

    public FluidStack getFluidToDrain() {
        Fluid fluid = filter.fluid(getWorld()).getFluid();
        int unit;
        if (Fluids.EMPTY.isSame(fluid)) {
            unit = 1;
            fluid = CEIFluids.EXPERIENCE.get();
        } else unit = ExperienceHelper.getExperienceFluidUnit(fluid);
        if (unit == 0)
            return FluidStack.EMPTY;
        int amount = count * POINTS_PER_SCROLL;
        amount = count == 0 ? Integer.MAX_VALUE : amount * unit;
        return new FluidStack(fluid, amount);
    }

    public FluidStack getFluidToFill(int available) {
        if (available == 0)
            return FluidStack.EMPTY;
        Fluid fluid = filter.fluid(getWorld()).getFluid();
        int unit;
        if (Fluids.EMPTY.isSame(fluid)) {
            unit = 1;
            fluid = CEIFluids.EXPERIENCE.get();
        } else unit = ExperienceHelper.getExperienceFluidUnit(fluid);
        if (unit == 0)
            return FluidStack.EMPTY;
        int amount = count * POINTS_PER_SCROLL;
        amount = count == 0 ? available : Math.min(available, amount * unit);
        return new FluidStack(fluid, amount);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.put("Filter", getFilter().serializeNBT());
        nbt.putInt("Scroll", count);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        filter = FilterItemStack.of(nbt.getCompound("Filter"));
        count = nbt.getInt("Scroll");
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = settings.value();
        blockEntity.setChanged();
        blockEntity.sendData();
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, count);
    }

    @Override
    public boolean isCountVisible() {
        return true;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
                CEILang.translate("gui.experience_hatch.exchange").component(),
                100,
                10,
                List.of(CEILang.translate("gui.experience_hatch.points").component()),
                new ValueSettingsFormatter(this::formatValue));
    }

    @Override
    public MutableComponent formatValue(ValueSettings value) {
        int count = value.value();
        if (count == 0)
            return CEILang.translate("gui.experience_hatch.all").component();
        return Component.literal(String.valueOf(count * POINTS_PER_SCROLL));
    }

    @Override
    public MutableComponent getCountLabelForValueBox() {
        if (count == 0)
            return Component.literal("*");
        return Component.literal(String.valueOf(count * POINTS_PER_SCROLL));
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        FilterItemStack filter = FilterItemStack.of(stack.copy());
        if (!filter.isEmpty()) {
            FluidStack fluid = filter.fluid(getWorld());
            if (fluid.getFluid() != CEIFluids.EXPERIENCE.get()
                    && CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid.getFluid()) == null)
                return false;
        }
        this.filter = filter;
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    @Override
    public String getClipboardKey() {
        return "ExperienceHatch";
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
