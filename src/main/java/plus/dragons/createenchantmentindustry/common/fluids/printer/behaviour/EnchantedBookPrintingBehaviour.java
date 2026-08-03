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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIIntIntPair;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class EnchantedBookPrintingBehaviour implements PrintingBehaviour {
    private final Level level;
    private final SmartFluidTankBehaviour tank;
    private final ItemStack original;
    private final Map<Enchantment, Integer> enchantments;
    private final int cost;

    private EnchantedBookPrintingBehaviour(Level level, SmartFluidTankBehaviour tank, ItemStack original, Map<Enchantment, Integer> enchantments) {
        this.level = level;
        this.tank = tank;
        this.original = original;
        this.enchantments = Map.copyOf(enchantments);
        int result = 0;
        for (var entry : enchantments.entrySet()) {
            Optional<CEIIntIntPair> optional = Optional.empty();
            var customCost = CEIDataMaps.PRINTING_ENCHANTED_BOOK_COST.get(entry.getKey());
            if (customCost != null) {
                optional = customCost.stream().filter(pair -> pair.level() == entry.getValue()).findFirst();
            }
            result += optional.map(CEIIntIntPair::value).orElseGet(() -> (int) (CEIEnchantmentHelper.getEnchantmentCost(entry.getKey(), entry.getValue()) * CEIConfig.fluids().printingEnchantedBookCostMultiplier.get()));
        }
        this.cost = result;
    }

    public static Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK))
            return Optional.empty();
        var enchantments = CEIItemData.getEnchantments(stack);
        if (enchantments.isEmpty())
            return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.enchanted_book.invalid")));
        if (enchantments.keySet().stream().anyMatch(enchantment -> isDenied(level, enchantment))) {
            if (CEIConfig.fluids().printingEnchantedBookDenylistStopCopying.get()) {
                return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.enchanted_book.denied")));
            } else {
                var allowed = new LinkedHashMap<Enchantment, Integer>();
                enchantments.forEach((enchantment, enchantmentLevel) -> {
                    if (!isDenied(level, enchantment))
                        allowed.put(enchantment, enchantmentLevel);
                });
                if (allowed.isEmpty())
                    return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.enchanted_book.all_denied")));
                else {
                    return Optional.of(DataResult.success(new EnchantedBookPrintingBehaviour(level, tank, stack, allowed)));
                }
            }
        }
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
        var result = CEIItemData.transmuteCopy(stack, Items.ENCHANTED_BOOK);
        result.setCount(1);
        CEIItemData.setEnchantments(result, enchantments);
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
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        enchantments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(registry::getId)))
                .forEach(entry -> CEILang.builder()
                        .add(entry.getKey().getFullname(entry.getValue()))
                        .forGoggles(tooltip, 1));
        return true;
    }

    private static boolean isDenied(Level level, Enchantment enchantment) {
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return registry.getResourceKey(enchantment)
                .flatMap(registry::getHolder)
                .map(holder -> holder.is(CEIEnchantments.MOD_TAGS.printingDeny))
                .orElse(false);
    }
}
