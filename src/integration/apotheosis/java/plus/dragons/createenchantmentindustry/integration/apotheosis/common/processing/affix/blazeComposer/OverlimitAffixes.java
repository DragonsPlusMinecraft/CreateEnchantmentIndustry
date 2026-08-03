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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record OverlimitAffixes(Map<DynamicHolder<Affix>, Float> levels) {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverlimitAffixes.class);
    public static final OverlimitAffixes EMPTY = new OverlimitAffixes(Map.of());
    public static final Codec<OverlimitAffixes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(AffixRegistry.INSTANCE.holderCodec(), Codec.floatRange(0, Float.MAX_VALUE))
                    .fieldOf("levels")
                    .forGetter(OverlimitAffixes::levels))
            .apply(instance, OverlimitAffixes::new));

    public boolean isEmpty() {
        return levels.isEmpty();
    }

    public float getLevel(DynamicHolder<? extends Affix> affix) {
        return levels.getOrDefault(affix, 0F);
    }

    public CompoundTag save() {
        Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(message -> LOGGER.warn("Unable to encode overlimit affix data: {}", message))
                .orElseGet(CompoundTag::new);
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static @Nullable OverlimitAffixes load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(message -> LOGGER.warn("Ignoring invalid overlimit affix data: {}", message))
                .orElse(null);
    }
}
