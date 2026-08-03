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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record AffixTemplateData(
        DynamicHolder<LootRarity> rarity,
        List<AffixTemplateEntry> entries) {
    private static final Logger LOGGER = LoggerFactory.getLogger(AffixTemplateData.class);

    public static final Codec<AffixTemplateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").forGetter(AffixTemplateData::rarity),
            AffixTemplateEntry.CODEC.listOf().fieldOf("entries").forGetter(AffixTemplateData::entries))
            .apply(instance, AffixTemplateData::new));

    public AffixTemplateData {
        entries = normalizeEntries(entries);
    }

    public static AffixTemplateData single(DynamicHolder<LootRarity> rarity, AffixTemplateEntry entry) {
        return new AffixTemplateData(rarity, List.of(entry));
    }

    public boolean isBound() {
        return rarity.isBound() && entries.stream().allMatch(AffixTemplateEntry::isBound);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public Optional<AffixTemplateEntry> get(DynamicHolder<Affix> affix) {
        return entries.stream()
                .filter(entry -> entry.affix().equals(affix))
                .findFirst();
    }

    public boolean contains(DynamicHolder<Affix> affix) {
        return get(affix).isPresent();
    }

    public AffixTemplateData withEntries(List<AffixTemplateEntry> entries) {
        return new AffixTemplateData(rarity, entries);
    }

    public CompoundTag save() {
        Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(message -> LOGGER.warn("Unable to encode affix template data: {}", message))
                .orElseGet(CompoundTag::new);
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static @Nullable AffixTemplateData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(message -> LOGGER.warn("Ignoring invalid affix template data: {}", message))
                .orElse(null);
    }

    private static List<AffixTemplateEntry> normalizeEntries(List<AffixTemplateEntry> entries) {
        if (entries.isEmpty())
            return List.of();
        LinkedHashMap<ResourceLocation, AffixTemplateEntry> merged = new LinkedHashMap<>();
        entries.stream()
                .sorted(Comparator.comparing(entry -> entry.affix().getId()))
                .forEach(entry -> {
                    ResourceLocation id = entry.affix().getId();
                    AffixTemplateEntry existing = merged.get(id);
                    if (existing == null) {
                        merged.put(id, entry);
                    } else {
                        merged.put(id, existing.mergeMetadata(entry, Math.max(existing.level(), entry.level())));
                    }
                });
        return List.copyOf(new ArrayList<>(merged.values()));
    }
}
