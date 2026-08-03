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
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.Collection;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.AffixLevelLimits;

public record AffixTemplateEntry(
        DynamicHolder<Affix> affix,
        float level,
        List<ResourceLocation> sourceCategories,
        boolean transcendent) {

    public static final Codec<AffixTemplateEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AffixRegistry.INSTANCE.holderCodec().fieldOf("affix").forGetter(AffixTemplateEntry::affix),
            Codec.floatRange(0, Float.MAX_VALUE).fieldOf("level").forGetter(AffixTemplateEntry::level),
            ResourceLocation.CODEC.listOf().optionalFieldOf("source_categories", List.of()).forGetter(AffixTemplateEntry::sourceCategories),
            Codec.BOOL.optionalFieldOf("transcendent", false).forGetter(AffixTemplateEntry::transcendent))
            .apply(instance, AffixTemplateEntry::new));
    public AffixTemplateEntry {
        level = Math.max(0, level);
        sourceCategories = normalizeSourceCategories(sourceCategories);
        transcendent = transcendent || level > AffixLevelLimits.EXTENDED_MAX_LEVEL;
    }

    public boolean isBound() {
        return affix.isBound();
    }

    public AffixTemplateEntry withLevel(float level) {
        return new AffixTemplateEntry(
                affix,
                level,
                sourceCategories,
                level > AffixLevelLimits.EXTENDED_MAX_LEVEL || transcendent);
    }

    public AffixTemplateEntry withSourceCategories(Collection<ResourceLocation> categories) {
        return new AffixTemplateEntry(affix, level, List.copyOf(categories), transcendent);
    }

    public AffixTemplateEntry mergeMetadata(AffixTemplateEntry other, float level) {
        return new AffixTemplateEntry(
                affix,
                level,
                normalizeSourceCategories(
                        java.util.stream.Stream.concat(sourceCategories.stream(), other.sourceCategories().stream())
                                .toList()),
                transcendent || other.transcendent());
    }

    public AffixInstance toInstance(DynamicHolder<LootRarity> rarity, ItemStack stack) {
        return new AffixInstance(affix, stack, rarity, level);
    }

    private static List<ResourceLocation> normalizeSourceCategories(List<ResourceLocation> categories) {
        if (categories.isEmpty())
            return List.of();
        return categories.stream()
                .distinct()
                .sorted()
                .toList();
    }
}
