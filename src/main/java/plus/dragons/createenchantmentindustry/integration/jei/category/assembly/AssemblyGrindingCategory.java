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
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.integration.jei.category.grinding.AnimatedGrindstone;

public class AssemblyGrindingCategory extends SequencedAssemblySubCategory {
    private final AnimatedGrindstone grindstone = new AnimatedGrindstone();

    public AssemblyGrindingCategory() {
        super(25);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
        List<FluidIngredient> fluidIngredients = recipe.getRecipe().getFluidIngredients();
        if (!fluidIngredients.isEmpty())
            CreateRecipeCategory.addFluidSlot(builder, x + 4, 15, fluidIngredients.get(0));
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack poseStack = graphics.pose();
        grindstone.offset = index;
        poseStack.pushPose();
        poseStack.translate(-7, 48.5f, 0);
        poseStack.scale(.6f, .6f, .6f);
        grindstone.draw(graphics, getWidth() / 2, 30);
        poseStack.popPose();
    }
}
