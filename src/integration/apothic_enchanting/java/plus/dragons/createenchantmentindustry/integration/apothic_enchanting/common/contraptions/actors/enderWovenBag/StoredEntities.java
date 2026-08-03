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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;

/** Immutable-on-write collection used by block, contraption, and item persistence. */
public class StoredEntities {
    private static final String LIST_KEY = "entities";
    private List<CompoundTag> entityTags;
    private final Map<Component, Integer> nameCache = new HashMap<>();

    public StoredEntities(List<CompoundTag> entityTags) {
        this.entityTags = entityTags.stream().map(CompoundTag::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public StoredEntities() {
        this(List.of());
    }

    public StoredEntities copy() {
        return new StoredEntities(entityTags);
    }

    public Map<Component, Integer> getEntityNames(Level level) {
        if (entityTags.isEmpty()) {
            return Map.of();
        }
        if (nameCache.isEmpty()) {
            for (CompoundTag tag : entityTags) {
                Entity entity = EntityType.loadEntityRecursive(tag, level, Function.identity());
                if (entity != null) {
                    nameCache.merge(entity.getName(), 1, Integer::sum);
                }
            }
        }
        return new LinkedHashMap<>(nameCache);
    }

    @Nullable
    public Entity pop(Level level) {
        if (entityTags.isEmpty()) {
            return null;
        }
        CompoundTag tag = entityTags.remove(entityTags.size() - 1);
        nameCache.clear();
        return EntityType.loadEntityRecursive(tag, level, Function.identity());
    }

    @Nullable
    public Entity peek(Level level) {
        if (entityTags.isEmpty()) {
            return null;
        }
        return EntityType.loadEntityRecursive(entityTags.get(entityTags.size() - 1), level, Function.identity());
    }

    public void push(Entity entity) {
        CompoundTag tag = new CompoundTag();
        if (entity.save(tag)) {
            entityTags.add(tag);
            nameCache.clear();
        }
    }

    public int count() {
        return entityTags.size();
    }

    public boolean full() {
        return count() >= CEIAConfig.server().utility().enderWovenBagCapacity.get();
    }

    public static StoredEntities parse(@Nullable Tag tag) {
        if (tag == null) {
            return new StoredEntities();
        }
        ListTag list;
        if (tag instanceof ListTag directList) {
            list = directList;
        } else if (tag instanceof CompoundTag compound) {
            if (compound.contains(LIST_KEY, Tag.TAG_LIST)) {
                list = compound.getList(LIST_KEY, Tag.TAG_COMPOUND);
            } else if (compound.contains("Entities", Tag.TAG_LIST)) {
                list = compound.getList("Entities", Tag.TAG_COMPOUND);
            } else {
                return new StoredEntities();
            }
        } else {
            return new StoredEntities();
        }
        List<CompoundTag> tags = new ArrayList<>(Math.min(list.size(), 256));
        for (int i = 0; i < list.size() && i < 256; i++) {
            tags.add(list.getCompound(i).copy());
        }
        return new StoredEntities(tags);
    }

    public CompoundTag tag() {
        CompoundTag result = new CompoundTag();
        ListTag list = new ListTag();
        entityTags.stream().limit(256).map(CompoundTag::copy).forEach(list::add);
        result.put(LIST_KEY, list);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof StoredEntities other && entityTags.equals(other.entityTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityTags);
    }
}
