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

package plus.dragons.createenchantmentindustry.integration.jei.category.assembly;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.AnimatedPrinter;

public class AssemblyPrintingCategory extends SequencedAssemblySubCategory {
    private final AnimatedPrinter printer = new AnimatedPrinter();

    public AssemblyPrintingCategory() {
        super(36);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
        builder.addSlot(RecipeIngredientRole.INPUT, x, 15)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getRecipe().getIngredients().get(1));
        FluidIngredient fluidIngredient = recipe.getRecipe()
                .getFluidIngredients()
                .get(0);
        CreateRecipeCategory.addFluidSlot(builder, x + 18, 15, fluidIngredient);
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        var fluid = recipe.getRecipe().getFluidIngredients().get(0).getMatchingFluidStacks().get(0);
        PoseStack poseStack = graphics.pose();
        printer.offset = index;
        poseStack.pushPose();
        poseStack.translate(-7, 50, 0);
        poseStack.scale(.75f, .75f, .75f);
        printer.withFluid(fluid).draw(graphics, getWidth() / 2, 0);
        poseStack.popPose();
    }
}
