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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyGrindingCategory;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class GrindingRecipe extends ProcessingRecipe<Container> implements IAssemblyRecipe {
    public GrindingRecipe(ProcessingRecipeParams params) {
        super(CEIRecipes.GRINDING, params);
        if (fluidIngredients.size() + fluidResults.size() > 1) {
            throw new IllegalArgumentException("Grinding recipe can only have either one fluid input or one fluid result");
        }
    }

    public static ProcessingRecipeBuilder<GrindingRecipe> builder(ResourceLocation id) {
        return new ProcessingRecipeBuilder<>(GrindingRecipe::new, id);
    }

    public static Optional<GrindingRecipe> fromPolishing(SandPaperPolishingRecipe recipe) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(recipe)) {
            return Optional.empty();
        }
        ResourceLocation id = new ResourceLocation(
                recipe.getId().getNamespace(), recipe.getId().getPath() + "_using_grindstone");
        GrindingRecipe grinding = builder(id)
                .require(recipe.getIngredients().get(0))
                .output(recipe.getRollableResults().get(0))
                .build();
        return Optional.of(grinding);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(Container input, Level level) {
        return !ingredients.isEmpty() && ingredients.get(0).test(input.getItem(0));
    }

    @Override
    public Component getDescriptionForAssembly() {
        if (fluidIngredients.isEmpty()) {
            return CEILang.translate("recipe.assembly.grinding").component();
        }
        List<FluidStack> matchingFluids = fluidIngredients.get(0).getMatchingFluidStacks();
        if (matchingFluids.isEmpty()) {
            return Component.literal("Invalid");
        }
        return CEILang.translate(
                "recipe.assembly.grinding.needs_fluid", matchingFluids.get(0).getDisplayName())
                .component();
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CEIBlocks.MECHANICAL_GRINDSTONE.get());
        required.add(AllBlocks.ITEM_DRAIN.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {}

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> AssemblyGrindingCategory::new;
    }
}
