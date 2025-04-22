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

package plus.dragons.createenchantmentindustry.data;

import static com.simibubi.create.AllBlocks.*;
import static com.simibubi.create.AllItems.*;
import static com.simibubi.create.AllTags.commonItemTag;
import static net.minecraft.world.item.Items.*;
import static net.neoforged.neoforge.common.Tags.Items.EGGS;
import static net.neoforged.neoforge.common.Tags.Items.STORAGE_BLOCKS_IRON;
import static plus.dragons.createdragonsplus.common.registry.CDPBlocks.FLUID_HATCH;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE;
import static plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders.*;
import static plus.dragons.createdragonsplus.data.recipe.VanillaRecipeBuilders.shaped;
import static plus.dragons.createdragonsplus.data.recipe.VanillaRecipeBuilders.shapeless;
import static plus.dragons.createenchantmentindustry.common.registry.CEIBlocks.*;
import static plus.dragons.createenchantmentindustry.common.registry.CEIFluids.EXPERIENCE;
import static plus.dragons.createenchantmentindustry.common.registry.CEIItems.*;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import plus.dragons.createdragonsplus.data.recipe.integration.IntegrationIngredient;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;

public class CEIRecipeProvider extends RecipeProvider {
    private static final String ANDESITE = "andesite";
    private static final String COPPER = "copper";
    private static final String BRASS = "brass";
    private static final String TRAIN = "train";

    public CEIRecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildMachineRecipes(output);
        buildMaterialRecipes(output);
        buildExperienceRecipes(output);
    }

    private void buildMachineRecipes(RecipeOutput output) {
        shaped().define('a', ANDESITE_ALLOY)
                .define('s', SHAFT)
                .pattern("aaa")
                .pattern("asa")
                .pattern("aaa")
                .output(MECHANICAL_GRINDSTONE)
                .unlockedBy(ANDESITE, has(ANDESITE_ALLOY))
                .accept(output);
        manualApplication(EXPERIENCE_HATCH.getId())
                .require(FLUID_HATCH)
                .require(EXPERIENCE_BLOCK)
                .output(EXPERIENCE_HATCH)
                .build(output);
        shaped().define('-', commonItemTag("plates/brass"))
                .define('o', SPOUT)
                .define('=', STORAGE_BLOCKS_IRON)
                .pattern("-")
                .pattern("o")
                .pattern("=")
                .output(PRINTER)
                .unlockedBy(BRASS, has(BRASS_INGOT))
                .accept(output);
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(BLAZE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(BLAZE_BURNER),
                Ingredient.of(ENCHANTING_TABLE),
                RecipeCategory.MISC,
                BLAZE_ENCHANTER.asItem())
                .unlocks("has_blaze_burner", has(BLAZE_BURNER))
                .save(output, BLAZE_ENCHANTER.getId().withPrefix("smithing/"));
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(BLAZE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(BLAZE_BURNER),
                Ingredient.of(ANVIL),
                RecipeCategory.MISC,
                BLAZE_FORGER.asItem())
                .unlocks("has_blaze_burner", has(BLAZE_BURNER))
                .save(output, BLAZE_FORGER.getId().withPrefix("smithing/"));
    }

    private void buildMaterialRecipes(RecipeOutput output) {
        shapeless().output(SUPER_EXPERIENCE_NUGGET, 9)
                .require(SUPER_EXPERIENCE_BLOCK)
                .accept(output);
        shaped().output(SUPER_EXPERIENCE_BLOCK)
                .define('n', SUPER_EXPERIENCE_NUGGET)
                .pattern("nnn")
                .pattern("nnn")
                .pattern("nnn")
                .accept(output);
        pressing(ENCHANTING_TEMPLATE.getId())
                .require(EXPERIENCE_BLOCK)
                .output(ENCHANTING_TEMPLATE)
                .build(output);
        pressing(SUPER_ENCHANTING_TEMPLATE.getId())
                .require(SUPER_EXPERIENCE_BLOCK)
                .output(SUPER_ENCHANTING_TEMPLATE)
                .build(output);
        compacting(EXPERIENCE_CAKE_BASE.getId())
                .require(EGGS)
                .require(SUGAR)
                .require(LAPIS_LAZULI)
                .output(EXPERIENCE_CAKE_BASE)
                .build(output);
        filling(EXPERIENCE_CAKE.getId())
                .require(EXPERIENCE_CAKE_BASE)
                .require(EXPERIENCE.get(), 1000)
                .output(EXPERIENCE_CAKE)
                .build(output);
        cutting(EXPERIENCE_CAKE_SLICE.getId())
                .require(EXPERIENCE_CAKE)
                .output(EXPERIENCE_CAKE_SLICE, 4)
                .build(output);
    }

    private void buildExperienceRecipes(RecipeOutput output) {
        compacting(CEICommon.asResource("experience_block"))
                .require(EXPERIENCE.get(), 27)
                .output(EXPERIENCE_BLOCK)
                .build(output);
        filling(CEICommon.asResource("experience_bottle"))
                .require(EXPERIENCE.get(), 10)
                .require(GLASS_BOTTLE)
                .output(EXPERIENCE_BOTTLE)
                .build(output);
        emptying(CEICommon.asResource("experience_bottle"))
                .require(EXPERIENCE_BOTTLE)
                .output(EXPERIENCE.get(), 10)
                .output(GLASS_BOTTLE)
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("experience_nugget"))
                .require(EXP_NUGGET)
                .output(EXPERIENCE.get(), 3)
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("experience_block"))
                .require(EXPERIENCE_BLOCK)
                .output(EXPERIENCE.get(), 27)
                .build(output);
        GrindingRecipe.builder(SUPER_EXPERIENCE_NUGGET.getId())
                .require(SUPER_EXPERIENCE_NUGGET)
                .output(EXPERIENCE.get(), 3)
                .build(output);
        GrindingRecipe.builder(SUPER_EXPERIENCE_BLOCK.getId())
                .require(SUPER_EXPERIENCE_BLOCK)
                .output(EXPERIENCE.get(), 27)
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("create_sa/heap_of_experience"))
                .whenModLoaded("create_sa")
                .require(IntegrationIngredient.of("create_sa", "heap_of_experience"))
                .output(EXPERIENCE.get(), 12)
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("ars_nouveau/experience_gem"))
                .whenModLoaded("ars_nouveau")
                .require(IntegrationIngredient.of("ars_nouveau", "experience_gem"))
                .output(EXPERIENCE.get(), 3)
                .build(output);
        GrindingRecipe.builder(CEICommon.asResource("ars_nouveau/greater_experience_gem"))
                .whenModLoaded("ars_nouveau")
                .require(IntegrationIngredient.of("ars_nouveau", "greater_experience_gem"))
                .output(EXPERIENCE.get(), 12)
                .build(output);
        emptying(CEICommon.asResource("mysticalagriculture/experience_droplet"))
                .whenModLoaded("mysticalagriculture")
                .require(IntegrationIngredient.of("mysticalagriculture", "experience_droplet"))
                .output(EXPERIENCE.get(), 10)
                .build(output);
    }
}
