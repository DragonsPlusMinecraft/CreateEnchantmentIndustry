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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.integration.apotheosis.config.CEIAXConfig;

/** Bridges the six native Apotheosis 1.20.1 loot rarities to the legacy gem-cutting presentation. */
public final class GemCutting {
    private GemCutting() {}

    public static Optional<Tier> tier(ItemStack stack) {
        GemInstance gem = GemInstance.unsocketed(stack);
        if (!gem.isValidUnsocketed() || !gem.rarity().isBound()) {
            return Optional.empty();
        }
        return Tier.byId(gem.rarity().getId());
    }

    public static boolean canCut(ItemStack stack) {
        return tier(stack).map(GemCutting::canCut).orElse(false);
    }

    public static boolean canCut(Tier tier) {
        return tier != Tier.ANCIENT;
    }

    public static Tier resultTier(Tier tier) {
        return canCut(tier) ? Tier.values()[tier.ordinal() + 1] : tier;
    }

    public static int getCutCost(Tier tier) {
        if (!canCut(tier)) {
            return 0;
        }
        int cost = switch (tier) {
            case COMMON -> CEIAXConfig.server().fluids().gemCutterCostCrackedToChipped.get();
            case UNCOMMON -> CEIAXConfig.server().fluids().gemCutterCostChippedToFlawed.get();
            case RARE -> CEIAXConfig.server().fluids().gemCutterCostFlawedToNormal.get();
            case EPIC -> CEIAXConfig.server().fluids().gemCutterCostNormalToFlawless.get();
            case MYTHIC -> CEIAXConfig.server().fluids().gemCutterCostFlawlessToPerfect.get();
            case ANCIENT -> 0;
        };
        return Math.max(1, Math.round(cost * CEIAXConfig.server().fluids().gemCutterCostMultiplier.getF()));
    }

    /** Returns one upgraded gem and leaves the caller responsible for preserving the input-stack remainder. */
    public static Optional<ItemStack> upgradeOne(ItemStack input, Tier expectedFrom) {
        Optional<Tier> actual = tier(input);
        if (actual.isEmpty() || actual.get() != expectedFrom || !canCut(expectedFrom)) {
            return Optional.empty();
        }
        Tier resultTier = resultTier(expectedFrom);
        DynamicHolder<LootRarity> resultRarity = resultTier.holder();
        if (!resultRarity.isBound()) {
            return Optional.empty();
        }
        ItemStack result = input.copy();
        result.setCount(1);
        AffixHelper.setRarity(result, resultRarity.get());
        return Optional.of(result);
    }

    public enum Tier {
        COMMON("common"),
        UNCOMMON("uncommon"),
        RARE("rare"),
        EPIC("epic"),
        MYTHIC("mythic"),
        ANCIENT("ancient");

        private final ResourceLocation id;

        Tier(String path) {
            this.id = new ResourceLocation("apotheosis", path);
        }

        public ResourceLocation id() {
            return id;
        }

        public DynamicHolder<LootRarity> holder() {
            return RarityRegistry.INSTANCE.holder(id);
        }

        /** Localized legacy gem quality plus its native 1.20.1 rarity, e.g. Flawless (Mythic). */
        public Component displayName() {
            return Component.translatable(
                    "create_enchantment_industry.gui.goggles.gem_cutter.tier." + id.getPath());
        }

        public static Optional<Tier> byId(ResourceLocation id) {
            return Arrays.stream(values()).filter(tier -> tier.id.equals(id)).findFirst();
        }
    }
}
