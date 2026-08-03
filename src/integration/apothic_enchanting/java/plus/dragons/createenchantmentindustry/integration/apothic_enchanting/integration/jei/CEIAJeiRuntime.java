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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.integration.jei;

import com.google.common.base.Preconditions;
import com.simibubi.create.AllBlocks;
import dev.shadowsoffire.apotheosis.Apoth;
import java.util.stream.Collectors;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.ApiStatus;
import plus.dragons.createdragonsplus.util.ErrorMessages;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusingRecipe;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIARecipes;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.integration.jei.category.InfusingCategory;

public class CEIAJeiRuntime {
    public static void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new InfusingCategory());
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = getRecipeManager();
        registration.addRecipes(InfusingCategory.TYPE, recipeManager.getAllRecipesFor(CEIARecipes.INFUSING.getType()));
        registration.addRecipes(InfusingCategory.TYPE, recipeManager
                .getAllRecipesFor(Apoth.RecipeTypes.INFUSION)
                .stream()
                .map(InfusingRecipe::createDisplayRecipe)
                .collect(Collectors.toList()));
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(InfusingCategory.TYPE, CEIABlocks.INFUSER, AllBlocks.BASIN);
    }

    @ApiStatus.Internal
    public static RecipeManager getRecipeManager() {
        if (FMLLoader.getDist() != Dist.CLIENT)
            throw new IllegalStateException("Retreiving recipe manager from client level is only supported for client");
        var minecraft = Minecraft.getInstance();
        Preconditions.checkNotNull(minecraft, ErrorMessages.notNull("minecraft"));
        var level = minecraft.level;
        Preconditions.checkNotNull(level, ErrorMessages.notNull("level"));
        return level.getRecipeManager();
    }
}
