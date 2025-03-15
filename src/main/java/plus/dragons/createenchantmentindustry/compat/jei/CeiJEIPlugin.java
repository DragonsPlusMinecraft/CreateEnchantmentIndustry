package plus.dragons.createenchantmentindustry.compat.jei;

import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.compat.jei.category.DisenchantingCategory;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.simibubi.create.compat.jei.CreateJEI.*;
import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CeiJEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = EnchantmentIndustry.genRL("jei_plugin");

    protected final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
    protected IIngredientManager ingredientManager;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
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

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        List<FluidStack> fluidIngredients = new ArrayList<>();
        fluidIngredients.add(new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), FluidType.BUCKET_VOLUME));
        fluidIngredients.add(new FluidStack(CeiFluids.HYPER_EXPERIENCE.get().getSource(), FluidType.BUCKET_VOLUME));
        jeiRuntime.getIngredientManager().addIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, fluidIngredients);
    }

    private <T extends Recipe<?>> RecipeCategoryBuilder<T> builder(Class<T> cls) {
        return new RecipeCategoryBuilder<>(cls);
    }

    private void loadCategories() {
        allCategories.clear();
        builder(DisenchantRecipe.class)
                .addTypedRecipes(CeiRecipeTypes.DISENCHANTING)
                .catalyst(CeiBlocks.DISENCHANTER::get)
                .emptyBackground(177, 50)
                .build("disenchanting", DisenchantingCategory::new);
    }

    private class RecipeCategoryBuilder<T extends Recipe<? extends RecipeInput>> {
        private final Class<? extends T> recipeClass;
        private Predicate<CRecipes> predicate = cRecipes -> true;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<RecipeHolder<T>>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public RecipeCategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public RecipeCategoryBuilder<T> enableIf(Predicate<CRecipes> predicate) {
            this.predicate = predicate;
            return this;
        }

        public RecipeCategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBase.ConfigBool> configValue) {
            predicate = c -> configValue.apply(c).get();
            return this;
        }

        public RecipeCategoryBuilder<T> addRecipeListConsumer(Consumer<List<RecipeHolder<T>>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public RecipeCategoryBuilder<T> addRecipes(Supplier<Collection<? extends RecipeHolder<T>>> collection) {
            return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
        }

        @SuppressWarnings("unchecked")
        public RecipeCategoryBuilder<T> addAllRecipesIf(Predicate<RecipeHolder<T>> pred) {
            return addRecipeListConsumer(recipes -> consumeAllRecipesOfType(recipe -> {
                if (pred.test(recipe))
                    recipes.add(recipe);
            }));
        }

        public RecipeCategoryBuilder<T> addAllRecipesIf(Predicate<RecipeHolder<?>> pred, Function<RecipeHolder<?>, RecipeHolder<T>> converter) {
            return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(converter.apply(recipe));
                }
            }));
        }

        public RecipeCategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }
        public <I extends RecipeInput, R extends Recipe<I>> RecipeCategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<R>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
                if (recipeClass.isInstance(recipe.value()))
                    //noinspection unchecked - checked by if statement above
                    recipes.add((RecipeHolder<T>) recipe);
            }, recipeType.get()));
        }

        public RecipeCategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<T>> recipeType, Function<RecipeHolder<?>, RecipeHolder<T>> converter) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
        }

        public RecipeCategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<RecipeHolder<?>> pred) {
            return addRecipeListConsumer(recipes -> consumeTypedRecipesTyped(recipe -> {
                if (pred.test(recipe)) {
                    recipes.add(recipe);
                }
            }, recipeType.get()));
        }

        public RecipeCategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
                                                                 Supplier<RecipeType<? extends T>> excluded) {
            return addRecipeListConsumer(recipes -> {
                List<RecipeHolder<?>> excludedRecipes = getTypedRecipes(excluded.get());
                consumeTypedRecipesTyped(recipe -> {
                    for (RecipeHolder<?> excludedRecipe : excludedRecipes) {
                        if (doInputsMatch(recipe.value(), excludedRecipe.value())) {
                            return;
                        }
                    }
                    recipes.add(recipe);
                }, recipeType.get());
            });
        }

        public RecipeCategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
            return addRecipeListConsumer(recipes -> {
                List<RecipeHolder<?>> excludedRecipes = getTypedRecipes(recipeType.get());
                recipes.removeIf(recipe -> {
                    for (RecipeHolder<?> excludedRecipe : excludedRecipes)
                        if (doInputsMatch(recipe.value(), excludedRecipe.value()) && doOutputsMatch(recipe.value(), excludedRecipe.value()))
                            return true;
                    return false;
                });
            });
        }

        public RecipeCategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public RecipeCategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public RecipeCategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public RecipeCategoryBuilder<T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public RecipeCategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public RecipeCategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public RecipeCategoryBuilder<T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {
            Supplier<List<RecipeHolder<T>>> recipesSupplier;
            if (predicate.test(AllConfigs.server().recipes)) {
                recipesSupplier = () -> {
                    List<RecipeHolder<T>> recipes = new ArrayList<>();
                    for (Consumer<List<RecipeHolder<T>>> consumer : recipeListConsumers)
                        consumer.accept(recipes);
                    return recipes;
                };
            } else {
                recipesSupplier = Collections::emptyList;
            }

            ResourceLocation id = EnchantmentIndustry.genRL(name);
            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
                    new mezz.jei.api.recipe.RecipeType<>(id, recipeClass),
                    LANG.fromRL("recipe", id).component(),
                    background, icon, recipesSupplier, catalysts);
            CreateRecipeCategory<T> category = factory.create(info);
            allCategories.add(category);
            return category;
        }

        private void consumeAllRecipesOfType(Consumer<RecipeHolder<T>> consumer) {
            consumeAllRecipes(recipeHolder -> {
                if (recipeClass.isInstance(recipeHolder.value())) {
                    //noinspection unchecked - this is checked by the if statement
                    consumer.accept((RecipeHolder<T>) recipeHolder);
                }
            });
        }

        private void consumeTypedRecipesTyped(Consumer<RecipeHolder<T>> consumer, RecipeType<?> type) {
            consumeTypedRecipes(recipeHolder -> {
                if (recipeClass.isInstance(recipeHolder.value())) {
                    //noinspection unchecked - this is checked by the if statement
                    consumer.accept((RecipeHolder<T>) recipeHolder);
                }
            }, type);
        }
    }
}
