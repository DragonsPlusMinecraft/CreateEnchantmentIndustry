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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class CEICodecs {
    public static final Codec<EnchantmentInstance> ENCHANTMENT_INSTANCE = RecordCodecBuilder
            .create(instance -> instance.group(
                    BuiltInRegistries.ENCHANTMENT.byNameCodec().fieldOf("id").forGetter(it -> it.enchantment),
                    Codec.intRange(0, 255).fieldOf("level").forGetter(it -> it.level)).apply(instance, EnchantmentInstance::new));
}
