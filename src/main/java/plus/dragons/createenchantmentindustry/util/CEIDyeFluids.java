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

package plus.dragons.createenchantmentindustry.util;

import java.util.Optional;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createdragonsplus.common.fluids.dye.DyeFluidType;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;

public class CEIDyeFluids {
    public static Fluid get(DyeColor color) {
        return CDPFluids.DYES_BY_VARIANT.get(variantId(color)).get();
    }

    public static ItemStack bucket(DyeColor color) {
        return CDPFluids.DYES_BY_VARIANT.get(variantId(color)).getBucket().get().getDefaultInstance();
    }

    public static TagKey<Fluid> tag(DyeColor color) {
        return CDPFluids.COMMON_TAGS.dyesByVariant.get(variantId(color));
    }

    public static Optional<DyeColor> color(FluidStack stack) {
        return color(stack.getFluid());
    }

    public static Optional<DyeColor> color(Fluid fluid) {
        if (fluid.getFluidType() instanceof DyeFluidType type)
            return color(type);
        return Optional.empty();
    }

    public static Optional<DyeColor> color(DyeFluidType type) {
        var variant = type.getVariant();
        if (variant.vanillaColor() != null)
            return Optional.of(variant.vanillaColor());
        return Optional.ofNullable(DyeColor.byName(variant.id().getPath(), null))
                .filter(color -> color.getId() > DyeColor.BLACK.getId());
    }

    public static Optional<Style> style(FluidStack stack) {
        if (stack.getFluid().getFluidType() instanceof DyeFluidType type)
            return Optional.of(Style.EMPTY.withColor(type.getVariant().color()));
        return Optional.empty();
    }

    private static ResourceLocation variantId(DyeColor color) {
        return new ResourceLocation(color.getName());
    }
}
