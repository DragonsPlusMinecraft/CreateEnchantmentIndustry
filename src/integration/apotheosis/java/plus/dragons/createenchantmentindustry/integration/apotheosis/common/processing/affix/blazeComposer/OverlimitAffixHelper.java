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

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.AffixLevelLimits;

/** Keeps 1.20's native {@code affix_data} valid while layering CEI levels above Apotheosis' level-one cap. */
public final class OverlimitAffixHelper {
    private OverlimitAffixHelper() {}

    public static Map<DynamicHolder<? extends Affix>, AffixInstance> applyTrueLevels(
            ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        OverlimitAffixes overlimit = get(stack);
        if (overlimit == null || overlimit.isEmpty() || affixes.isEmpty())
            return affixes;
        Map<DynamicHolder<? extends Affix>, AffixInstance> result = null;
        for (var entry : affixes.entrySet()) {
            float trueLevel = overlimit.getLevel(entry.getKey());
            if (trueLevel > entry.getValue().level()) {
                if (result == null)
                    result = new HashMap<>(affixes);
                var instance = entry.getValue();
                result.put(
                        entry.getKey(),
                        new AffixInstance(instance.affix(), instance.stack(), instance.rarity(), trueLevel));
            }
        }
        return result == null ? affixes : Map.copyOf(result);
    }

    public static float getTrueLevel(
            ItemStack stack, DynamicHolder<? extends Affix> affix, float fallback) {
        OverlimitAffixes overlimit = get(stack);
        return overlimit == null ? fallback : Math.max(fallback, overlimit.getLevel(affix));
    }

    public static void setAffixLevel(
            ItemStack stack, DynamicHolder<? extends Affix> affix, float level) {
        setAffixLevels(stack, Map.of(affix, level));
    }

    public static void setAffixLevels(
            ItemStack stack, Map<? extends DynamicHolder<? extends Affix>, Float> changedLevels) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> nativeAffixes = new HashMap<>();
        AffixHelper.getAffixes(stack).forEach((holder, instance) -> nativeAffixes.put(
                holder,
                instance.withNewLevel(Math.min(
                        instance.level(), AffixLevelLimits.NATIVE_STORAGE_MAX_LEVEL))));

        var rarity = AffixHelper.getRarity(stack);
        for (var entry : changedLevels.entrySet()) {
            DynamicHolder<Affix> affix = normalize(entry.getKey());
            float level = entry.getValue();
            if (level <= 0) {
                nativeAffixes.remove(affix);
            } else {
                nativeAffixes.put(
                        affix,
                        new AffixInstance(
                                affix,
                                stack,
                                rarity,
                                Math.min(level, AffixLevelLimits.NATIVE_STORAGE_MAX_LEVEL)));
            }
        }
        AffixHelper.setAffixes(stack, nativeAffixes);

        Map<DynamicHolder<Affix>, Float> levels = new HashMap<>();
        OverlimitAffixes old = get(stack);
        if (old != null)
            levels.putAll(old.levels());
        for (var entry : changedLevels.entrySet()) {
            DynamicHolder<Affix> affix = normalize(entry.getKey());
            float level = entry.getValue();
            if (level > AffixLevelLimits.NATIVE_STORAGE_MAX_LEVEL) {
                levels.put(affix, level);
            } else {
                levels.remove(affix);
            }
        }
        set(stack, levels.isEmpty() ? null : new OverlimitAffixes(Map.copyOf(levels)));
    }

    public static void removeAffix(ItemStack stack, DynamicHolder<? extends Affix> affix) {
        setAffixLevel(stack, affix, 0);
    }

    public static void clear(ItemStack stack) {
        set(stack, null);
    }

    private static DynamicHolder<Affix> normalize(DynamicHolder<? extends Affix> affix) {
        return AffixRegistry.INSTANCE.holder(affix.getId());
    }

    private static @Nullable OverlimitAffixes get(ItemStack stack) {
        var tag = CEIItemData.getOwnedData(stack, CEIItemData.OVERLIMIT_AFFIXES_TAG);
        return tag == null ? null : OverlimitAffixes.load(tag);
    }

    private static void set(ItemStack stack, @Nullable OverlimitAffixes value) {
        CEIItemData.setOwnedData(
                stack,
                CEIItemData.OVERLIMIT_AFFIXES_TAG,
                value == null || value.isEmpty() ? null : value.save());
    }
}
