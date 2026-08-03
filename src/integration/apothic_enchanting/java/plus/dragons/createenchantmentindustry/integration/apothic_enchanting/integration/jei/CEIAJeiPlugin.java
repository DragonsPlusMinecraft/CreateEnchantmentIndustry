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

import dev.shadowsoffire.apotheosis.Apotheosis;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import plus.dragons.createenchantmentindustry.common.CEICommon;

@JeiPlugin
public class CEIAJeiPlugin implements IModPlugin {
    public static final ResourceLocation ID = CEICommon.asResource("ceia_jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (ModList.get().isLoaded("apotheosis") && Apotheosis.enableEnch)
            CEIAJeiRuntime.registerCategories(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (ModList.get().isLoaded("apotheosis") && Apotheosis.enableEnch)
            CEIAJeiRuntime.registerRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (ModList.get().isLoaded("apotheosis") && Apotheosis.enableEnch)
            CEIAJeiRuntime.registerRecipeCatalysts(registration);
    }
}
