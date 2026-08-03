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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;

public record ExperienceFuel(int experience, boolean special, Optional<ItemStack> usingConvertTo) {

    public static final Codec<ExperienceFuel> INLINE_CODEC = ExtraCodecs.POSITIVE_INT.flatComapMap(
            ExperienceFuel::normal,
            fuel -> !fuel.special && fuel.usingConvertTo.isEmpty()
                    ? DataResult.success(fuel.experience())
                    : DataResult.error(() -> "ExperienceFuel " + fuel + " can not be encoded inline"));
    public static final Codec<ExperienceFuel> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("experience").forGetter(ExperienceFuel::experience),
            Codec.BOOL.optionalFieldOf("special", false).forGetter(ExperienceFuel::special),
            ItemStack.CODEC.optionalFieldOf("using_convert_to").forGetter(ExperienceFuel::usingConvertTo)).apply(instance, ExperienceFuel::new));
    public static final Codec<ExperienceFuel> CODEC = Codec.either(INLINE_CODEC, FULL_CODEC).xmap(
            either -> either.map(fuel -> fuel, fuel -> fuel),
            fuel -> !fuel.special && fuel.usingConvertTo.isEmpty() ? Either.left(fuel) : Either.right(fuel));
    public static ExperienceFuel normal(int experience) {
        return new ExperienceFuel(experience, false, Optional.empty());
    }

    public static ExperienceFuel normal(int experience, ItemStack usingConvertTo) {
        return new ExperienceFuel(experience, false, Optional.of(usingConvertTo));
    }

    public static ExperienceFuel special(int experience) {
        return new ExperienceFuel(experience, true, Optional.empty());
    }

    public static ExperienceFuel special(int experience, ItemStack usingConvertTo) {
        return new ExperienceFuel(experience, true, Optional.of(usingConvertTo));
    }

    public static @Nullable ExperienceFuel get(Level level, ItemStack stack) {
        var fuel = CEIDataMaps.EXPERIENCE_FUEL.get(stack.getItem());
        if (fuel != null)
            return fuel;
        if (!GenericItemEmptying.canItemBeEmptied(level, stack))
            return null;
        var emptying = GenericItemEmptying.emptyItem(level, stack, true);
        var fluid = emptying.getFirst();
        var experience = ExperienceHelper.getExperienceFromFluid(fluid);
        if (experience == 0)
            return null;
        var item = emptying.getSecond();
        return normal(experience, item);
    }
}
