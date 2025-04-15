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

package plus.dragons.createenchantmentindustry.integration.jei.category.printing;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createdragonsplus.util.Pairs;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;

public enum PatternPrintingRecipeJEI implements PrintingRecipeJEI {
    INSTANCE;

    public static final Type TYPE = PrintingRecipeJEI
            .register(CEICommon.asResource("pattern"), MapCodec.unit(INSTANCE));

    @Override
    public void setBase(IRecipeSlotBuilder slot) {
        slot.addItemStack(PackageStyles.getDefaultBox());
    }

    @Override
    public void setTemplate(IRecipeSlotBuilder slot) {
        for (PackageItem item : PackageStyles.ALL_BOXES) {
            var stack = new ItemStack(item);
            slot.addItemStack(stack);
        }
    }

    @Override
    public void setFluid(IRecipeSlotBuilder slot) {
        CEIDataMaps.getSourceFluidEntries(CEIDataMaps.PRINTING_PATTERN_INGREDIENT)
                .forEach(Pairs.accept(slot::addFluidStack));
    }

    @Override
    public void setOutput(IRecipeSlotBuilder slot) {
        PackageStyles.ALL_BOXES.forEach(slot::addItemLike);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void onDisplayedIngredientsUpdate(IRecipeSlotDrawable baseSlot, IRecipeSlotDrawable templateSlot, IRecipeSlotDrawable fluidSlot, IRecipeSlotDrawable outputSlot, IFocusGroup focuses) {
        List<IFocus<?>> outputFocuses = focuses.getFocuses(RecipeIngredientRole.OUTPUT).toList();
        ItemStack override;
        IRecipeSlotDrawable overridenSlot;
        if (outputFocuses.isEmpty()) {
            override = templateSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
            overridenSlot = outputSlot;
        } else {
            override = outputSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
            overridenSlot = templateSlot;
        }
        overridenSlot.createDisplayOverrides().addItemStack(override);
    }
}
