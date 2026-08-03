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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.fan.salvaging;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXRecipes;

public class SalvagingRecipe extends ProcessingRecipe<Container> {
    public SalvagingRecipe(ProcessingRecipeParams params) {
        super(CEIAXRecipes.SALVAGING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    @Override
    public boolean matches(Container input, Level level) {
        return !ingredients.isEmpty() && ingredients.get(0).test(input.getItem(0));
    }

    public static ProcessingRecipeBuilder<SalvagingRecipe> builder(ResourceLocation id) {
        return new ProcessingRecipeBuilder<>(SalvagingRecipe::new, id);
    }
}
