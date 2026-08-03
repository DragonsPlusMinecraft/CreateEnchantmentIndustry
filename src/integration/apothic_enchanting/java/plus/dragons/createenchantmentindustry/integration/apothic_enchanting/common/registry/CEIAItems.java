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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry;

import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.data.tag.ItemTagRegistry;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.data.CEIConditionalLootTables;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

public class CEIAItems {
    private static final TagKey<Item> FORGE_BUCKETS = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "buckets"));
    public static final ModTags MOD_TAGS = new ModTags();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_BRASS_BOOKSHELF = REGISTRATE
            .item("incomplete_brass_bookshelf", SequencedAssemblyItem::new)
            .model((ctx, prov) -> prov
                    .cubeColumn(ctx.getName(), prov.modLoc("block/brass_bookshelf_top"), prov.modLoc("block/brass_bookshelf_bottom")))
            .register();

    public static class ModTags extends ItemTagRegistry {
        public ModTags() {
            super(CEIACommon.ID);
            addOptional(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag, ResourceLocation.fromNamespaceAndPath("apotheosis", "infused_breath"));
            addOptional(FORGE_BUCKETS, CEICommon.asResource("infused_dragon_breath_bucket"));
            addOptional(CDPItems.COMMON_TAGS.dragonBreathBuckets, CEICommon.asResource("infused_dragon_breath_bucket"));
            addOptionalBlockDrop("infuser");
            addOptionalBlockDrop("brass_bookshelf");
            addOptionalBlockDrop("creative_bookshelf");
            addOptionalBlockDrop("ender_woven_bag");
        }

        private void addOptionalBlockDrop(String path) {
            ResourceLocation item = CEICommon.asResource(path);
            addOptional(TagKey.create(Registries.ITEM, CEIConditionalLootTables.optionalDropTag(item)), item);
        }
    }

    public static void register() {
        REGISTRATE.registerItemTags(MOD_TAGS);
    }
}
