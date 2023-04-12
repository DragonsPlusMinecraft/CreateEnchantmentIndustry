package plus.dragons.createenchantmentindustry.compat.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackHelper;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackListFactory;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.compat.jei.category.DisenchantingCategory;
import plus.dragons.createenchantmentindustry.compat.jei.category.RecipeCategoryBuilder;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class CeiJEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = EnchantmentIndustry.genRL("jei_plugin");
    protected final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
    protected IIngredientManager ingredientManager;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        ISubtypeManager subtypeManager = registration.getSubtypeManager();
        IColorHelper colorHelper = registration.getColorHelper();

        List<FluidStack> fluidStacks = new ArrayList<>();
        fluidStacks.add(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), FluidAttributes.BUCKET_VOLUME));
        fluidStacks.add(new FluidStack(CeiFluids.HYPER_EXPERIENCE.get().getSource(), FluidAttributes.BUCKET_VOLUME));
        FluidStackHelper fluidStackHelper = new FluidStackHelper(subtypeManager, colorHelper);
        FluidStackRenderer fluidStackRenderer = new FluidStackRenderer();
        registration.register(ForgeTypes.FLUID_STACK, fluidStacks, fluidStackHelper, fluidStackRenderer);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories(registration);
        registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ingredientManager = registration.getIngredientManager();
        allCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach(c -> c.registerCatalysts(registration));
    }

    private static <T extends Recipe<?>> RecipeCategoryBuilder<T> builder(Class<T> cls) {
        return new RecipeCategoryBuilder<>(EnchantmentIndustry.ID, cls);
    }

    private void loadCategories(IRecipeCategoryRegistration registration) {
        allCategories.clear();
        allCategories.add(
                builder(DisenchantRecipe.class)
                        .addTypedRecipes(CeiRecipeTypes.DISENCHANTING)
                        .catalyst(CeiBlocks.DISENCHANTER::get)
                        .emptyBackground(177, 50)
                        .build("disenchanting", DisenchantingCategory::new)
        );
    }
}
