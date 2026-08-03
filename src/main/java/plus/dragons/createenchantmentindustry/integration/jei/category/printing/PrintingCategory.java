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

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.getRenderedSlot;

import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import java.util.List;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.mixin.accessor.CreateRecipeCategoryAccessor;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class PrintingCategory implements IRecipeCategory<PrintingRecipeJEI> {
    public static final RecipeType<PrintingRecipeJEI> TYPE = new RecipeType<>(CEIRecipes.PRINTING.getId(), PrintingRecipeJEI.class);
    private final Component title = CEILang.translate("recipe.printing").component();
    private final IDrawable icon = new ItemIcon(CEIBlocks.PRINTER::asStack);
    private final AnimatedPrinter printer = new AnimatedPrinter();

    @Override
    public RecipeType<PrintingRecipeJEI> getRecipeType() {
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
    public @Nullable ResourceLocation getRegistryName(PrintingRecipeJEI recipe) {
        return recipe.getRegistryName();
    }

    @SuppressWarnings("removal") // See CreateRecipeCategory#addPotionTooltip
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PrintingRecipeJEI recipe, IFocusGroup focuses) {
        var base = builder.addSlot(RecipeIngredientRole.INPUT, 27, 51)
                .setBackground(getRenderedSlot(), -1, -1);
        recipe.setBase(base);
        var template = builder.addSlot(RecipeIngredientRole.CATALYST, 51, 5)
                .setBackground(getRenderedSlot(), -1, -1);
        recipe.setTemplate(template);
        var fluid = builder.addSlot(RecipeIngredientRole.INPUT, 27, 32)
                .setBackground(getRenderedSlot(), -1, -1)
                .setFluidRenderer(1, false, 16, 16)
                .addTooltipCallback(CreateRecipeCategoryAccessor::invokeAddPotionTooltip);
        recipe.setFluid(fluid);
        var output = builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 51)
                .setBackground(getRenderedSlot(), -1, -1);
        recipe.setOutput(output);
    }

    @Override
    public void onDisplayedIngredientsUpdate(PrintingRecipeJEI recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
        var base = recipeSlots.get(0);
        var template = recipeSlots.get(1);
        var fluid = recipeSlots.get(2);
        var output = recipeSlots.get(3);
        recipe.onDisplayedIngredientsUpdate(base, template, fluid, output, focuses);
    }

    @Override
    public void draw(PrintingRecipeJEI recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
        var fluid = recipeSlotsView.getSlotViews().get(2)
                .getDisplayedIngredient(ForgeTypes.FLUID_STACK)
                .orElse(FluidStack.EMPTY);
        printer.withFluid(fluid).draw(graphics, getWidth() / 2 - 13, 22);
    }
}
