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

package plus.dragons.createenchantmentindustry.common.datamap;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;

/**
 * A Forge 1.20.1 representation of a NeoForge data-map type.
 *
 * <p>The type only describes the registry, id and codec. Values live in the immutable snapshots managed by
 * {@link CEIDataMaps}; keeping those responsibilities separate prevents partially applied reloads.</p>
 */
public final class CEIDataMapType<K, V> {
    private final ResourceLocation id;
    private final Registry<K> registry;
    private final Codec<V> codec;
    private final ResourceLocation resource;

    public CEIDataMapType(ResourceLocation id, Registry<K> registry, Codec<V> codec) {
        this.id = Objects.requireNonNull(id);
        this.registry = Objects.requireNonNull(registry);
        this.codec = Objects.requireNonNull(codec);
        this.resource = new ResourceLocation(
                id.getNamespace(),
                "data_maps/" + registry.key().location().getPath() + "/" + id.getPath() + ".json");
    }

    public ResourceLocation id() {
        return id;
    }

    public Registry<K> registry() {
        return registry;
    }

    public Codec<V> codec() {
        return codec;
    }

    public ResourceLocation resource() {
        return resource;
    }

    public V get(K key) {
        return CEIDataMaps.get(this, key);
    }

    public Stream<Pair<K, V>> entries() {
        return CEIDataMaps.entries(this);
    }
}
