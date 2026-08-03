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

package plus.dragons.createenchantmentindustry.integration.jei.category.printing;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import plus.dragons.createdragonsplus.util.Pairs;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIIntIntPair;

public class EnchantedBookPrintingRecipeJEI implements PrintingRecipeJEI {
    public static final PrintingRecipeJEI.Type TYPE = PrintingRecipeJEI
            .register(CEICommon.asResource("enchanted_book"));
    private final ResourceLocation id;
    private final Enchantment enchantment;
    private final int level;

    public EnchantedBookPrintingRecipeJEI(ResourceLocation enchantmentId, Enchantment enchantment, int level) {
        this.id = PrintingRecipeJEI.super.getRegistryName().withSuffix("/" +
                enchantmentId.getNamespace() + "/" +
                enchantmentId.getPath() + "/" +
                level);
        this.enchantment = enchantment;
        this.level = level;
    }

    public static List<PrintingRecipeJEI> listAll() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return List.of();
        var registry = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        return registry.entrySet().stream()
                .filter(entry -> registry.getHolder(entry.getKey())
                        .map(holder -> !holder.is(CEIEnchantments.MOD_TAGS.printingDeny))
                        .orElse(true))
                .flatMap(entry -> IntStream
                        .rangeClosed(entry.getValue().getMinLevel(), CEIEnchantmentHelper.maxLevel(entry.getValue()))
                        .mapToObj(level -> new EnchantedBookPrintingRecipeJEI(
                                entry.getKey().location(), entry.getValue(), level)))
                .collect(Collectors.toList());
    }

    private Optional<ItemStack> createEnchantmentBook() {
        return Optional.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
    }

    private OptionalInt getCost() {
        Optional<CEIIntIntPair> custom = Optional.empty();
        var customCosts = CEIDataMaps.PRINTING_ENCHANTED_BOOK_COST.get(enchantment);
        if (customCosts != null)
            custom = customCosts.stream().filter(pair -> pair.level() == level).findFirst();
        int baseCost = custom.map(CEIIntIntPair::value)
                .orElseGet(() -> CEIEnchantmentHelper.getEnchantmentCost(enchantment, level));
        return OptionalInt.of((int) (baseCost * CEIConfig.fluids().printingEnchantedBookCostMultiplier.get()));
    }

    @Override
    public void setBase(IRecipeSlotBuilder slot) {
        slot.addItemLike(Items.BOOK);
    }

    @Override
    public void setTemplate(IRecipeSlotBuilder slot) {
        createEnchantmentBook().ifPresent(slot::addItemStack);
    }

    @Override
    public void setFluid(IRecipeSlotBuilder slot) {
        getCost().ifPresent(cost -> {
            slot.addFluidStack(CEIFluids.EXPERIENCE.get(), cost);
            CEIDataMaps.getSourceFluidEntries(CEIDataMaps.FLUID_UNIT_EXPERIENCE)
                    .forEach(Pairs.accept((fluid, unit) -> slot.addFluidStack(fluid, (long) unit * cost)));
        });
    }

    @Override
    public void setOutput(IRecipeSlotBuilder slot) {
        createEnchantmentBook().ifPresent(slot::addItemStack);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return id;
    }
}
