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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry;

import java.util.function.Supplier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createdragonsplus.common.recipe.RecipeTypeInfo;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfusingRecipe;

public class CEIARecipes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CEIACommon.ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CEIACommon.ID);

    public static final RecipeTypeInfo<InfusingRecipe> INFUSING = register("infusing", () -> new InfusingRecipe.Serializer<>(InfusingRecipe::new));

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
    }

    private static <R extends Recipe<?>> RecipeTypeInfo<R> register(String name, Supplier<? extends RecipeSerializer<R>> serializer) {
        return new RecipeTypeInfo<>(name, serializer, SERIALIZERS, TYPES);
    }
}
