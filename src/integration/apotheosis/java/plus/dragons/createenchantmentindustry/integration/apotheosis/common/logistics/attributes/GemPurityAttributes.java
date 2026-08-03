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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.logistics.attributes;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutting;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutting.Tier;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXItemAttributes;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

/** Create filter attribute for the six official Apotheosis 1.20.1 gem rarities. */
public final class GemPurityAttributes implements ItemAttribute {
    private ResourceLocation rarityId;

    public GemPurityAttributes(ResourceLocation rarityId) {
        this.rarityId = rarityId;
    }

    @Override
    public boolean appliesTo(ItemStack stack, Level world) {
        return GemCutting.tier(stack).map(tier -> tier.id().equals(rarityId)).orElse(false);
    }

    @Override
    public ItemAttributeType getType() {
        return CEIAXItemAttributes.GEM_PURITY.get();
    }

    @Override
    public void save(CompoundTag nbt) {
        nbt.putString("rarity", rarityId.toString());
    }

    @Override
    public void load(CompoundTag nbt) {
        ResourceLocation parsed = ResourceLocation.tryParse(nbt.getString("rarity"));
        rarityId = parsed == null ? Tier.COMMON.id() : parsed;
    }

    @Override
    public String getTranslationKey() {
        return CEIACommon.ID + ".gem_purity";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[] { Tier.byId(rarityId).orElse(Tier.COMMON).displayName() };
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new GemPurityAttributes(Tier.COMMON.id());
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
            return GemCutting.tier(stack)
                    .<List<ItemAttribute>>map(tier -> List.of(new GemPurityAttributes(tier.id())))
                    .orElseGet(List::of);
        }
    }
}
