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
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createdragonsplus.util.Pairs;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.util.CEIDyeFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

public enum CustomNamePrintingRecipeJEI implements PrintingRecipeJEI {
    INSTANCE;

    public static final PrintingRecipeJEI.Type TYPE = PrintingRecipeJEI
            .register(CEICommon.asResource("custom_name"), MapCodec.unit(INSTANCE));

    @Override
    public void setBase(IRecipeSlotBuilder slot) {
        slot.addItemLike(Items.NAME_TAG);
        slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(CEILang
                .translate("recipe.printing.custom_name.base")
                .style(ChatFormatting.GRAY)
                .component()));
    }

    @Override
    public void setTemplate(IRecipeSlotBuilder slot) {
        var stack = new ItemStack(Items.NAME_TAG);
        var name = CEILang.translate("recipe.printing.custom_name.template").component();
        CEIItemData.setCustomName(stack, name);
        slot.addItemStack(stack);
    }

    @Override
    public void setFluid(IRecipeSlotBuilder slot) {
        CEIDataMaps.getSourceFluidEntries(CEIDataMaps.PRINTING_CUSTOM_NAME_INGREDIENT)
                .forEach(Pairs.accept(slot::addFluidStack));
    }

    @Override
    public void setOutput(IRecipeSlotBuilder slot) {
        slot.addItemLike(Items.NAME_TAG);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void onDisplayedIngredientsUpdate(IRecipeSlotDrawable baseSlot, IRecipeSlotDrawable templateSlot, IRecipeSlotDrawable fluidSlot, IRecipeSlotDrawable outputSlot, IFocusGroup focuses) {
        var name = CEILang.translate("recipe.printing.custom_name.template").component();
        var fluidStack = fluidSlot.getDisplayedIngredient(ForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
        var style = CEIDataMaps.PRINTING_CUSTOM_NAME_STYLE.get(fluidStack.getFluid());
        if (style == null)
            style = CEIDyeFluids.style(fluidStack).orElse(null);
        if (style != null)
            name.withStyle(style);
        var stack = new ItemStack(Items.NAME_TAG);
        CEIItemData.setCustomName(stack, name);
        outputSlot.createDisplayOverrides().addItemStack(stack);
    }
}
