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
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class WrittenBookPrintingBehaviour implements PrintingBehaviour {
    private final SmartFluidTankBehaviour tank;
    private final CompoundTag content;

    private WrittenBookPrintingBehaviour(SmartFluidTankBehaviour tank, CompoundTag content) {
        this.tank = tank;
        this.content = content;
    }

    public static Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        if (!stack.is(Items.WRITTEN_BOOK))
            return Optional.empty();
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.getList("pages", Tag.TAG_STRING).isEmpty())
            return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.written_book.invalid")));
        int generation = tag.getInt("generation");
        int change = CEIConfig.fluids().printingGenerationChange.get();
        int newGeneration = Math.max(0, generation + change);
        if (newGeneration > 2)
            return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.written_book.invalid")));
        CompoundTag content = tag.copy();
        content.putInt("generation", newGeneration);
        return Optional.of(DataResult.success(new WrittenBookPrintingBehaviour(tank, content)));
    }

    private OptionalInt getCost(FluidStack fluid) {
        int cost = this.content.getList("pages", Tag.TAG_STRING).size();
        cost *= Objects.requireNonNullElse(CEIDataMaps.PRINTING_WRITTEN_BOOK_INGREDIENT.get(fluid.getFluid()), 0);
        if (cost == 0)
            return OptionalInt.empty();
        return OptionalInt.of(cost);
    }

    @Override
    public boolean isValid() {
        var fluid = tank.getPrimaryHandler().getFluid();
        if (fluid.isEmpty())
            return true;
        var cost = getCost(fluid);
        return cost.isPresent() && cost.getAsInt() <= CEIConfig.fluids().printerFluidCapacity.get();
    }

    @Override
    public boolean isSafeNBT() {
        return false; // Written books can contain contents unvailable for survival
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        if (stack.is(Items.BOOK))
            return 1;
        return 0;
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        return getCost(fluidStack).orElse(0);
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        var result = CEIItemData.transmuteCopy(stack, Items.WRITTEN_BOOK);
        result.setCount(1);
        CompoundTag resultTag = result.getOrCreateTag();
        resultTag.putString("title", content.getString("title"));
        resultTag.putString("author", content.getString("author"));
        resultTag.putInt("generation", content.getInt("generation"));
        resultTag.putBoolean("resolved", content.getBoolean("resolved"));
        resultTag.put("pages", content.getList("pages", Tag.TAG_STRING).copy());
        return result;
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        // Plays SoundEvents.BOOK_PAGE_TURN
        level.levelEvent(1043, pos.below(), 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEILang.translate("gui.goggles.printing").forGoggles(tooltip);
        CEILang.builder().add(Component.literal(content.getString("title")))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.builder().add(Component.translatable("book.byAuthor", content.getString("author")))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        getCost(tank.getPrimaryHandler().getFluid()).ifPresent(cost -> CEILang.translate("gui.goggles.printing.cost",
                CEILang.number(cost)
                        .add(CreateLang.translate("generic.unit.millibuckets"))
                        .style(cost <= CEIConfig.fluids().printerFluidCapacity.get()
                                ? ChatFormatting.GREEN
                                : ChatFormatting.RED))
                .forGoggles(tooltip, 1));
        return true;
    }
}
