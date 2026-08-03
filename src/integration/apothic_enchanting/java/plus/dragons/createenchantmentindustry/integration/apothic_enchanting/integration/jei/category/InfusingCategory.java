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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.integration.jei.category;

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.*;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusingRecipe;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIAFluids;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIARecipes;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.integration.jei.widget.AnimatedInfuser;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;
import plus.dragons.createenchantmentindustry.mixin.accessor.CreateRecipeCategoryAccessor;

public class InfusingCategory implements IRecipeCategory<InfusingRecipe> {
    public static final RecipeType<InfusingRecipe> TYPE = new RecipeType<>(
            CEIARecipes.INFUSING.getId(), InfusingRecipe.class);
    private static final Component title = CEIALang.translate("recipe.infusing").component();
    private static final IDrawable icon = new DoubleItemIcon(CEIABlocks.INFUSER::asStack, AllBlocks.BASIN::asStack);
    private static final ResourceLocation TEXTURES = new ResourceLocation(
            "apotheosis", "textures/gui/enchanting_jei.png");
    private final AnimatedInfuser infuser = new AnimatedInfuser();

    @Override
    public RecipeType<InfusingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 177;
    }

    @Override
    public int getHeight() {
        return 66;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @SuppressWarnings("removal") // See CreateRecipeCategory#addPotionTooltip
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusingRecipe recipe, IFocusGroup focuses) {
        int x = 36;
        int y = 37;
        if (!recipe.getIngredients().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(recipe.getIngredients().get(0));
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(ForgeTypes.FLUID_STACK, recipe.getFluidIngredients().get(0).getMatchingFluidStacks())
                    .setFluidRenderer(1, false, 16, 16)
                    .addTooltipCallback(CreateRecipeCategoryAccessor::invokeAddPotionTooltip);
        }

        var reagent = FluidIngredient.fromTag(
                CEIAFluids.MOD_TAGS.infusing_ingredients,
                ApothMiscUtil.getExpCostForSlot((int) recipe.getStats().eterna(), 0));
        builder.addSlot(RecipeIngredientRole.INPUT, 120, 7)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(ForgeTypes.FLUID_STACK, reagent.getMatchingFluidStacks())
                .setFluidRenderer(1, false, 16, 16)
                .addTooltipCallback(CreateRecipeCategoryAccessor::invokeAddPotionTooltip);

        x = 142;
        if (!recipe.getRollableResults().isEmpty()) {
            var result = recipe.getRollableResults().get(0);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(getRenderedSlot(result), -1, -1)
                    .addItemStack(result.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(result));
        } else {
            var fluid = recipe.getFluidResults().get(0);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addFluidStack(fluid.getFluid(), fluid.getAmount())
                    .setFluidRenderer(1, false, 16, 16)
                    .addTooltipCallback(CreateRecipeCategoryAccessor::invokeAddPotionTooltip);
        }
    }

    @Override
    public void draw(InfusingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(guiGraphics, 136, 18);
        AllGuiTextures shadow = AllGuiTextures.JEI_SHADOW;
        shadow.render(guiGraphics, 79, 54);

        var fluid = recipeSlotsView.getSlotViews().get(1)
                .getDisplayedIngredient(ForgeTypes.FLUID_STACK)
                .orElse(FluidStack.EMPTY);
        infuser.with(fluid, recipe.getStats()).draw(guiGraphics, getWidth() / 2 + 3, 20);

        guiGraphics.blit(TEXTURES, 0, 0, 5, 26, 7, 7, 256, 256);
        guiGraphics.blit(TEXTURES, 0, 10, 5, 36, 7, 7, 256, 256);
        guiGraphics.blit(TEXTURES, 0, 20, 5, 46, 7, 7, 256, 256);

        var stats = recipe.getStats();
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, Component.translatable("gui.apotheosis.enchant.eterna").append(Component.literal(" " + stats.eterna())), 9, 0, 0x3DB53D, false);
        guiGraphics.drawString(font, Component.translatable("gui.apotheosis.enchant.quanta").append(Component.literal(" " + stats.quanta() + "%")), 9, 10, 0xFC5454, false);
        guiGraphics.drawString(font, Component.translatable("gui.apotheosis.enchant.arcana").append(Component.literal(" " + stats.arcana() + "%")), 9, 20, 0xA800A8, false);
    }
}
