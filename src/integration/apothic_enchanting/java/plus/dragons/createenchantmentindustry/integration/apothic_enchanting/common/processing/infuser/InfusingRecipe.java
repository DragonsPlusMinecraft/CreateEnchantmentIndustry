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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser;

import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIAFluids;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIARecipes;

/** Create processing wrapper for native CEI recipes and Apotheosis 7 enchanting recipes. */
public class InfusingRecipe extends ProcessingRecipe<Container> {
    private InfusionStats stats = InfusionStats.EMPTY;

    public InfusingRecipe(ProcessingRecipeParams params) {
        super(CEIARecipes.INFUSING, params);
    }

    public InfusionStats getStats() {
        return stats;
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    public static boolean match(InfuserBlockEntity infuser, BasinBlockEntity basin, Recipe<?> recipe) {
        return process(infuser, basin, recipe, true);
    }

    public static boolean apply(InfuserBlockEntity infuser, BasinBlockEntity basin, Recipe<?> recipe) {
        return process(infuser, basin, recipe, false);
    }

    private static boolean process(
            InfuserBlockEntity infuser, BasinBlockEntity basin, Recipe<?> recipe, boolean simulateOnly) {
        if (recipe instanceof InfusingRecipe infusingRecipe) {
            return processNative(infuser, basin, infusingRecipe, simulateOnly);
        }
        if (recipe instanceof EnchantingRecipe enchantingRecipe) {
            return processApotheosis(infuser, basin, enchantingRecipe, simulateOnly);
        }
        return false;
    }

    private static boolean processNative(
            InfuserBlockEntity infuser, BasinBlockEntity basin, InfusingRecipe recipe, boolean simulateOnly) {
        if (!recipe.stats.qualified(infuser.infusionStats)) {
            return false;
        }

        IItemHandler availableItems = getItemHandler(basin);
        IFluidHandler availableFluids = getFluidHandler(basin);
        IFluidHandler reagentTank = infuser.getFluidHandler(null);
        if (reagentTank == null) {
            return false;
        }

        int inputSlot = -1;
        FluidStack fluidInput = FluidStack.EMPTY;
        if (!recipe.ingredients.isEmpty()) {
            if (availableItems == null) {
                return false;
            }
            inputSlot = findMatchingItemSlot(availableItems, recipe.ingredients.get(0));
            if (inputSlot < 0) {
                return false;
            }
        } else if (!recipe.fluidIngredients.isEmpty()) {
            if (availableFluids == null) {
                return false;
            }
            fluidInput = findMatchingFluid(availableFluids, recipe.fluidIngredients.get(0));
            if (fluidInput.isEmpty()) {
                return false;
            }
        } else {
            return false;
        }

        List<ItemStack> outputItems = new ArrayList<>(recipe.rollResults());
        List<FluidStack> outputFluids = recipe.getFluidResults().stream()
                .filter(stack -> !stack.isEmpty())
                .map(FluidStack::copy)
                .toList();
        if (!matchesFilter(basin.getFilter(), outputItems, outputFluids)
                || !basin.acceptOutputs(outputItems, outputFluids, true)) {
            return false;
        }

        int requiredAmount = ApothMiscUtil.getExpCostForSlot((int) recipe.stats.eterna(), 0);
        FluidStack reagent = findInfusingIngredient(reagentTank, requiredAmount);
        if (reagent.isEmpty() || simulateOnly) {
            return !reagent.isEmpty();
        }

        ItemStack extractedItem = ItemStack.EMPTY;
        FluidStack drainedInput = FluidStack.EMPTY;
        if (inputSlot >= 0) {
            extractedItem = availableItems.extractItem(inputSlot, 1, false);
            if (!recipe.ingredients.get(0).test(extractedItem)) {
                rollbackItem(availableItems, inputSlot, extractedItem);
                return false;
            }
        } else {
            drainedInput = availableFluids.drain(fluidInput, IFluidHandler.FluidAction.EXECUTE);
            if (!sameFluidAndAmount(fluidInput, drainedInput)) {
                rollbackFluid(availableFluids, drainedInput);
                return false;
            }
        }

        FluidStack drainedReagent = reagentTank.drain(reagent, IFluidHandler.FluidAction.EXECUTE);
        if (!sameFluidAndAmount(reagent, drainedReagent)) {
            rollbackItem(availableItems, inputSlot, extractedItem);
            rollbackFluid(availableFluids, drainedInput);
            rollbackFluid(reagentTank, drainedReagent);
            return false;
        }
        if (basin.acceptOutputs(outputItems, outputFluids, false)) {
            return true;
        }

        rollbackItem(availableItems, inputSlot, extractedItem);
        rollbackFluid(availableFluids, drainedInput);
        rollbackFluid(reagentTank, drainedReagent);
        return false;
    }

    private static boolean processApotheosis(
            InfuserBlockEntity infuser, BasinBlockEntity basin, EnchantingRecipe recipe, boolean simulateOnly) {
        IItemHandler availableItems = getItemHandler(basin);
        IFluidHandler reagentTank = infuser.getFluidHandler(null);
        if (availableItems == null || reagentTank == null) {
            return false;
        }

        int inputSlot = findMatchingItemSlot(availableItems, recipe, infuser.infusionStats);
        if (inputSlot < 0) {
            return false;
        }
        ItemStack input = availableItems.extractItem(inputSlot, 1, true);
        ItemStack output = recipe.assemble(
                input,
                infuser.infusionStats.eterna(),
                infuser.infusionStats.quanta(),
                infuser.infusionStats.arcana());
        if (output.isEmpty()
                || !matchesFilter(basin.getFilter(), List.of(output), List.of())
                || !basin.acceptOutputs(List.of(output), List.of(), true)) {
            return false;
        }

        int requiredAmount = ApothMiscUtil.getExpCostForSlot((int) recipe.getRequirements().eterna(), 0);
        FluidStack reagent = findInfusingIngredient(reagentTank, requiredAmount);
        if (reagent.isEmpty() || simulateOnly) {
            return !reagent.isEmpty();
        }

        ItemStack extracted = availableItems.extractItem(inputSlot, 1, false);
        if (!recipe.matches(
                extracted,
                infuser.infusionStats.eterna(),
                infuser.infusionStats.quanta(),
                infuser.infusionStats.arcana())) {
            rollbackItem(availableItems, inputSlot, extracted);
            return false;
        }
        FluidStack drainedReagent = reagentTank.drain(reagent, IFluidHandler.FluidAction.EXECUTE);
        if (!sameFluidAndAmount(reagent, drainedReagent)) {
            rollbackItem(availableItems, inputSlot, extracted);
            rollbackFluid(reagentTank, drainedReagent);
            return false;
        }
        if (basin.acceptOutputs(List.of(output), List.of(), false)) {
            return true;
        }

        rollbackItem(availableItems, inputSlot, extracted);
        rollbackFluid(reagentTank, drainedReagent);
        return false;
    }

    private static @Nullable IItemHandler getItemHandler(BasinBlockEntity basin) {
        return basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }

    private static @Nullable IFluidHandler getFluidHandler(BasinBlockEntity basin) {
        return basin.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
    }

    private static int findMatchingItemSlot(IItemHandler items, Ingredient ingredient) {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (ingredient.test(items.extractItem(slot, 1, true))) {
                return slot;
            }
        }
        return -1;
    }

    private static int findMatchingItemSlot(IItemHandler items, EnchantingRecipe recipe, InfusionStats stats) {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack input = items.extractItem(slot, 1, true);
            if (recipe.matches(input, stats.eterna(), stats.quanta(), stats.arcana())) {
                return slot;
            }
        }
        return -1;
    }

    private static FluidStack findMatchingFluid(IFluidHandler fluids, FluidIngredient ingredient) {
        for (int tank = 0; tank < fluids.getTanks(); tank++) {
            FluidStack available = fluids.getFluidInTank(tank);
            if (available.isEmpty()) {
                continue;
            }
            FluidStack requested = available.copy();
            requested.setAmount(ingredient.getRequiredAmount());
            if (!ingredient.test(requested)) {
                continue;
            }
            FluidStack drained = fluids.drain(requested, IFluidHandler.FluidAction.SIMULATE);
            if (sameFluidAndAmount(requested, drained)) {
                return requested;
            }
        }
        return FluidStack.EMPTY;
    }

    private static FluidStack findInfusingIngredient(IFluidHandler fluids, int amount) {
        for (int tank = 0; tank < fluids.getTanks(); tank++) {
            FluidStack available = fluids.getFluidInTank(tank);
            if (!available.getFluid().is(CEIAFluids.MOD_TAGS.infusing_ingredients)) {
                continue;
            }
            FluidStack requested = available.copy();
            requested.setAmount(amount);
            FluidStack drained = fluids.drain(requested, IFluidHandler.FluidAction.SIMULATE);
            if (sameFluidAndAmount(requested, drained)) {
                return requested;
            }
        }
        return FluidStack.EMPTY;
    }

    private static boolean sameFluidAndAmount(FluidStack expected, FluidStack actual) {
        return !actual.isEmpty() && expected.isFluidStackIdentical(actual);
    }

    private static void rollbackItem(@Nullable IItemHandler handler, int preferredSlot, ItemStack stack) {
        if (handler == null || stack.isEmpty()) {
            return;
        }
        ItemStack remainder = preferredSlot >= 0 ? handler.insertItem(preferredSlot, stack, false) : stack;
        for (int slot = 0; !remainder.isEmpty() && slot < handler.getSlots(); slot++) {
            remainder = handler.insertItem(slot, remainder, false);
        }
    }

    private static void rollbackFluid(@Nullable IFluidHandler handler, FluidStack stack) {
        if (handler != null && !stack.isEmpty()) {
            handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private static boolean matchesFilter(
            FilteringBehaviour filter, List<ItemStack> itemOutputs, List<FluidStack> fluidOutputs) {
        if (filter == null) {
            return false;
        }
        if (!itemOutputs.isEmpty()) {
            return filter.test(itemOutputs.get(0));
        }
        if (!fluidOutputs.isEmpty()) {
            return filter.test(fluidOutputs.get(0));
        }
        return false;
    }

    public static boolean canProcessInput(Recipe<?> recipe, ItemStack stack) {
        if (recipe instanceof EnchantingRecipe enchantingRecipe) {
            return enchantingRecipe.getInput().test(stack);
        }
        return !recipe.getIngredients().isEmpty() && recipe.getIngredients().get(0).test(stack);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public void readAdditional(JsonObject json) {
        JsonObject statsJson = GsonHelper.getAsJsonObject(json, "stats");
        stats = new InfusionStats(
                GsonHelper.getAsFloat(statsJson, "eterna"),
                GsonHelper.getAsFloat(statsJson, "quanta"),
                GsonHelper.getAsFloat(statsJson, "arcana"));
    }

    @Override
    public void writeAdditional(JsonObject json) {
        JsonObject statsJson = new JsonObject();
        statsJson.addProperty("eterna", stats.eterna());
        statsJson.addProperty("quanta", stats.quanta());
        statsJson.addProperty("arcana", stats.arcana());
        json.add("stats", statsJson);
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        stats = InfusionStats.read(buffer);
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        stats.write(buffer);
    }

    public static class Builder extends ProcessingRecipeBuilder<InfusingRecipe> {
        private final InfusionStats stats;

        public Builder(ResourceLocation recipeId, InfusionStats stats) {
            super(InfusingRecipe::new, recipeId);
            this.stats = stats;
        }

        @Override
        public InfusingRecipe build() {
            InfusingRecipe recipe = super.build();
            recipe.stats = stats;
            return recipe;
        }
    }

    public static class Serializer<R extends InfusingRecipe> extends ProcessingRecipeSerializer<R> {
        public Serializer(ProcessingRecipeBuilder.ProcessingRecipeFactory<R> factory) {
            super(factory);
        }
    }

    public static InfusingRecipe createDisplayRecipe(EnchantingRecipe recipe) {
        var requirements = recipe.getRequirements();
        InfusionStats stats = new InfusionStats(requirements.eterna(), requirements.quanta(), requirements.arcana());
        return new Builder(recipe.getId(), stats)
                .withItemIngredients(recipe.getInput())
                .withSingleItemOutput(recipe.getOutput())
                .build();
    }
}
