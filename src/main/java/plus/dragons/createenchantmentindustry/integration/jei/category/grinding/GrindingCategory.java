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

package plus.dragons.createenchantmentindustry.integration.jei.category.grinding;

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.*;

import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class GrindingCategory implements IRecipeCategory<GrindingRecipe> {
    public static final RecipeType<GrindingRecipe> TYPE = new RecipeType<>(
            CEIRecipes.GRINDING.getId(), GrindingRecipe.class);
    private final Component title = CEILang.translate("recipe.grinding").component();
    private final IDrawable icon = new ItemIcon(CEIBlocks.GRINDSTONE_DRAIN::asStack);
    private final AnimatedGrindstone grindstone = new AnimatedGrindstone();

    @Override
    public RecipeType<GrindingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 177;
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        var ingredient = recipe.getIngredients().get(0);
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 32)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(ingredient);

        var fluidIngredients = recipe.getFluidIngredients();
        if (!fluidIngredients.isEmpty()) {
            CreateRecipeCategory.addFluidSlot(builder, 27, 51, fluidIngredients.get(0));
        }

        List<ProcessingOutput> results = recipe.getRollableResults();
        int i = 0;
        var fluidResults = recipe.getFluidResults();
        if (!fluidResults.isEmpty()) {
            CreateRecipeCategory.addFluidSlot(builder, 130, 32, fluidResults.get(0));
            i++;
        }
        for (ProcessingOutput output : results) {
            int xOffset = i % 2 == 0 ? 0 : 19;
            int yOffset = (i / 2) * -19;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 130 + xOffset, 32 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            i++;
        }
    }

    @Override
    public void draw(GrindingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 115, 5);
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 52);
        grindstone.draw(graphics, 68, 32);
    }
}
