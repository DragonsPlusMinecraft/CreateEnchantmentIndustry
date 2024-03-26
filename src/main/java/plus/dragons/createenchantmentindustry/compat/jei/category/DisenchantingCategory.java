package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DisenchantingCategory extends CreateRecipeCategory<DisenchantRecipe> {
    
    private final IDrawable disenchanter = new DisenchanterDrawable();
    
    public DisenchantingCategory(Info<DisenchantRecipe> info) {
        super(info);
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisenchantRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 25)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(recipe.getIngredients().get(0));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 25)
                .setBackground(getRenderedSlot(), -1, -1)
                .addFluidStack(CeiFluids.EXPERIENCE.get().getSource(),recipe.getExperience())
                .addTooltipCallback(addFluidTooltip(recipe.getExperience()));

        if(!recipe.hasNoResult())
            builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 5)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public void draw(DisenchantRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(guiGraphics, 62, 31);
        disenchanter.draw(guiGraphics, getBackground().getWidth() / 2 - 13, 16);
    }
}