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
import static net.minecraft.world.item.Items.*;
import static net.minecraftforge.common.Tags.Items.EGGS;
import static net.minecraftforge.common.Tags.Items.STORAGE_BLOCKS_IRON;
import static plus.dragons.createdragonsplus.common.registry.CDPBlocks.FLUID_HATCH;
import static plus.dragons.createdragonsplus.common.registry.CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE;
import static plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders.*;
import static plus.dragons.createdragonsplus.data.recipe.VanillaRecipeBuilders.shaped;
import static plus.dragons.createdragonsplus.data.recipe.VanillaRecipeBuilders.shapeless;
import static plus.dragons.createenchantmentindustry.common.registry.CEIBlocks.*;
import static plus.dragons.createenchantmentindustry.common.registry.CEIFluids.EXPERIENCE;
import static plus.dragons.createenchantmentindustry.common.registry.CEIItems.*;

import com.simibubi.create.foundation.data.recipe.CommonMetal;
import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import plus.dragons.createdragonsplus.data.recipe.integration.IntegrationIngredient;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class CEIRecipeProvider extends RecipeProvider {
    private static final String ANDESITE = "andesite";
    private static final String COPPER = "copper";
    private static final String BRASS = "brass";
    private static final String TRAIN = "train";

    public CEIRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        buildMachineRecipes(output);
        buildMaterialRecipes(output);
        buildExperienceRecipes(output);
    }

    private void buildMachineRecipes(Consumer<FinishedRecipe> output) {
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
        shaped().define('-', CommonMetal.BRASS.plates)
                .define('o', SPOUT)
                .define('=', STORAGE_BLOCKS_IRON)
                .pattern("-")
                .pattern("o")
                .pattern("=")
                .output(PRINTER)
                .unlockedBy(BRASS, has(BRASS_INGOT))
                .accept(output);
        shaped().define('a', EXPERIENCE_BLOCK)
                .define('s', SPONGE)
                .define('c', COPPER_CASING)
                .pattern("a")
                .pattern("s")
                .pattern("c")
                .output(EXPERIENCE_LANTERN)
                .unlockedBy(COPPER, has(COPPER_CASING))
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
        var classicId = CLASSIC_BLAZE_ENCHANTER.getId().withPrefix("smithing/");
        ConditionalRecipe.builder()
                .addCondition(CEIConfig.features().classicBlazeEnchanter)
                .addRecipe(consumer -> SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(BLAZE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(BLAZE_BURNER),
                        Ingredient.of(BLAZES_ENCHANTING_HANDBOOK),
                        RecipeCategory.MISC,
                        CLASSIC_BLAZE_ENCHANTER.asItem())
                        .unlocks("has_blaze_burner", has(BLAZE_BURNER))
                        .save(consumer, classicId))
                .generateAdvancement()
                .build(output, classicId);
    }

    private void buildMaterialRecipes(Consumer<FinishedRecipe> output) {
        shapeless().output(SUPER_EXPERIENCE_NUGGET, 9)
                .require(SUPER_EXPERIENCE_BLOCK)
                .unlockedBy("has_super_experience_block", has(SUPER_EXPERIENCE_BLOCK))
                .accept(output);
        shaped().output(SUPER_EXPERIENCE_BLOCK)
                .define('n', SUPER_EXPERIENCE_NUGGET)
                .pattern("nnn")
                .pattern("nnn")
                .pattern("nnn")
                .unlockedBy("has_super_experience_nugget", has(SUPER_EXPERIENCE_NUGGET))
                .accept(output);
        pressing(ENCHANTING_TEMPLATE.getId())
                .require(EXPERIENCE_BLOCK)
                .output(ENCHANTING_TEMPLATE)
                .build(output);
        pressing(SUPER_ENCHANTING_TEMPLATE.getId())
                .require(SUPER_EXPERIENCE_BLOCK)
                .output(SUPER_ENCHANTING_TEMPLATE)
                .build(output);
        shapeless().output(BLAZES_ENCHANTING_HANDBOOK)
                .require(BLAZE_UPGRADE_SMITHING_TEMPLATE)
                .require(STURDY_SHEET)
                .require(STURDY_SHEET)
                .require(EXPERIENCE_BOTTLE)
                .require(EXPERIENCE_BOTTLE)
                .require(MAGMA_BLOCK)
                .unlockedBy("has_blaze_burner", has(BLAZE_BURNER))
                .withCondition(CEIConfig.features().classicBlazeEnchanter)
                .accept(output);
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

    private void buildExperienceRecipes(Consumer<FinishedRecipe> output) {
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
        crushing(CEICommon.asResource("infested_cobblestone"))
                .require(Blocks.INFESTED_COBBLESTONE)
                .output(Blocks.GRAVEL)
                .output(0.5f, EXP_NUGGET.asStack())
                .build(output);
        compacting(CEICommon.asResource("infested_stone"))
                .require(Blocks.INFESTED_STONE).require(Blocks.INFESTED_STONE)
                .require(Blocks.INFESTED_STONE).require(Blocks.INFESTED_STONE)
                .output(Blocks.STONE_BRICKS)
                .output(EXP_NUGGET.asStack())
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
