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

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;

public class StandardPrintingRecipeJEI implements PrintingRecipeJEI {
    public static final PrintingRecipeJEI.Type TYPE = PrintingRecipeJEI
            .register(CEICommon.asResource("standard"));
    private final PrintingRecipe recipe;

    public StandardPrintingRecipeJEI(PrintingRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setBase(IRecipeSlotBuilder slot) {
        slot.addIngredients(recipe.getIngredients().get(0));
    }

    @Override
    public void setTemplate(IRecipeSlotBuilder slot) {
        slot.addIngredients(recipe.getIngredients().get(1));
    }

    @Override
    public void setFluid(IRecipeSlotBuilder slot) {
        var fluid = recipe.getFluidIngredients().get(0);
        slot.addIngredients(ForgeTypes.FLUID_STACK, fluid.getMatchingFluidStacks());
    }

    @Override
    public void setOutput(IRecipeSlotBuilder slot) {
        slot.addItemStack(recipe.getRollableResults().get(0).getStack());
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getId();
    }
}
