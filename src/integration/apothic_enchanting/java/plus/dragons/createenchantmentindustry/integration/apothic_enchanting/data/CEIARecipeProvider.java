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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.data;

import static com.simibubi.create.AllBlocks.*;
import static com.simibubi.create.AllItems.*;
import static plus.dragons.createdragonsplus.data.recipe.VanillaRecipeBuilders.shaped;
import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks.*;
import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIAItems.INCOMPLETE_BRASS_BOOKSHELF;

import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import dev.shadowsoffire.apotheosis.ench.Ench;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.crafting.CEIApotheosisModuleCondition;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusingRecipe;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusionStats;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIAFluids;

public class CEIARecipeProvider extends RecipeProvider {
    private static final String BRASS = "brass";
    private static final Item ENDER_LEAD = Objects.requireNonNull(
            ForgeRegistries.ITEMS.getValue(new ResourceLocation("apotheosis", "ender_lead")));

    public CEIARecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        shaped().define('-', BRASS_SHEET)
                .define('o', SPOUT)
                .define('=', ORANGE_NIXIE_TUBE)
                .pattern(" - ")
                .pattern(" o ")
                .pattern("===")
                .output(INFUSER)
                .withCondition(ModIntegration.APOTHIC_ENCHANTING.condition())
                .withCondition(CEIApotheosisModuleCondition.ENCHANTMENT)
                .unlockedBy(BRASS, has(BRASS_INGOT))
                .accept(output);

        shaped().define('-', BRASS_CASING)
                .define('o', ENDER_LEAD)
                .define('=', PRECISION_MECHANISM)
                .define('x', ROSE_QUARTZ_LAMP)
                .pattern("oxo")
                .pattern("o=o")
                .pattern("o-o")
                .output(ENDER_WOVEN_BAG)
                .withCondition(ModIntegration.APOTHIC_ENCHANTING.condition())
                .withCondition(CEIApotheosisModuleCondition.ENCHANTMENT)
                .unlockedBy(BRASS, has(BRASS_INGOT))
                .accept(output);

        new InfusingRecipe.Builder(CEICommon.asResource("infused_dragon_breath"), new InfusionStats(80, 15, 60))
                .withCondition(ModIntegration.APOTHIC_ENCHANTING.condition())
                .withCondition(CEIApotheosisModuleCondition.ENCHANTMENT)
                .require(CDPFluids.DRAGON_BREATH.get().getSource(), 250)
                .output(new FluidStack(CEIAFluids.INFUSED_DRAGON_BREATH.get(), 750))
                .build(output);

        var brassBookshelf = CreateRecipeBuilders.sequencedAssembly(BRASS_BOOKSHELF.getId())
                .require(Ench.Blocks.PEARL_ENDSHELF.get())
                .transitionTo(INCOMPLETE_BRASS_BOOKSHELF)
                .addOutput(BRASS_BOOKSHELF, 1)
                .loops(3)
                .addStep(DeployerApplicationRecipe::new,
                        rb -> rb.require(BRASS_INGOT))
                .addStep(FillingRecipe::new, rb -> rb.require(CEIAFluids.MOD_TAGS.infusing_ingredients, 250))
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(PRECISION_MECHANISM));
        ConditionalRecipe.builder()
                .addCondition(ModIntegration.APOTHIC_ENCHANTING.condition())
                .addCondition(CEIApotheosisModuleCondition.ENCHANTMENT)
                .addRecipe(brassBookshelf::build)
                .build(output, BRASS_BOOKSHELF.getId().withPrefix("sequenced_assembly/"));
    }
}
