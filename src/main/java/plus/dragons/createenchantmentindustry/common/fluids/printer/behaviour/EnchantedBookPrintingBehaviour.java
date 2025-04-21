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
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class EnchantedBookPrintingBehaviour implements PrintingBehaviour {
    private final Level level;
    private final SmartFluidTankBehaviour tank;
    private final ItemStack original;
    private final ItemEnchantments enchantments;
    private final int cost;

    private EnchantedBookPrintingBehaviour(Level level, SmartFluidTankBehaviour tank, ItemStack original, ItemEnchantments enchantments) {
        this.level = level;
        this.tank = tank;
        this.original = original;
        this.enchantments = enchantments;
        this.cost = CEIEnchantmentHelper.getEnchantmentCost(enchantments);
    }

    public static Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK))
            return Optional.empty();
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty())
            return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.enchanted_book.invalid")));
        return Optional.of(DataResult.success(new EnchantedBookPrintingBehaviour(level, tank, stack, enchantments)));
    }

    private OptionalInt getCost(FluidStack fluid) {
        int cost = this.cost;
        cost = ExperienceHelper.getFluidFromExperience(fluid, cost);
        if (cost == 0)
            return OptionalInt.empty();
        return OptionalInt.of(cost);
    }

    @Override
    public boolean isValid() {
        if (this.cost <= 0)
            return false;
        var fluid = tank.getPrimaryHandler().getFluid();
        if (fluid.isEmpty())
            return true;
        var cost = getCost(fluid);
        return cost.isPresent() && cost.getAsInt() <= CEIConfig.fluids().printerFluidCapacity.get();
    }

    @Override
    public boolean isSafeNBT() {
        return false; // Prevent getting enchantments from nowhere
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
        var result = stack.transmuteCopy(Items.ENCHANTED_BOOK, 1);
        result.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
        return result;
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        level.playSound(null, pos.below(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS,
                1.0F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEILang.translate("gui.goggles.printing").forGoggles(tooltip);
        CEILang.item(original).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        getCost(tank.getPrimaryHandler().getFluid()).ifPresent(cost -> CEILang.translate("gui.goggles.printing.cost",
                CEILang.number(cost)
                        .add(CreateLang.translate("generic.unit.millibuckets"))
                        .style(cost <= CEIConfig.fluids().printerFluidCapacity.get()
                                ? ChatFormatting.GREEN
                                : ChatFormatting.RED))
                .style(ChatFormatting.GRAY).forGoggles(tooltip));
        HolderLookup.Provider registries = level.registryAccess();
        var order = registries
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.TOOLTIP_ORDER)
                .map(holders -> (HolderSet<Enchantment>) holders)
                .orElse(HolderSet.direct());
        for (Holder<Enchantment> ordered : order) {
            int level = enchantments.getLevel(ordered);
            if (level > 0) {
                CEILang.builder().add(Enchantment.getFullname(ordered, level)).forGoggles(tooltip, 1);
            }
        }
        for (Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> unordered = entry.getKey();
            int level = entry.getIntValue();
            if (level > 0 && !order.contains(unordered)) {
                CEILang.builder().add(Enchantment.getFullname(unordered, level)).forGoggles(tooltip, 1);
            }
        }
        return true;
    }
}
