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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry;

import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import plus.dragons.createdragonsplus.data.tag.ItemTagRegistry;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.data.CEIConditionalLootTables;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateItem;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

public class CEIAXItems {
    private static final TagKey<Item> FORGE_BUCKETS = TagKey.create(
            Registries.ITEM, new ResourceLocation("forge", "buckets"));
    public static final ModTags MOD_TAGS = new ModTags();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_BRASS_AFFIX_TEMPLATE = REGISTRATE
            .item("incomplete_brass_affix_template", SequencedAssemblyItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                    .texture("layer0", prov.modLoc("item/brass_affix_template")))
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_CRYSTAL_AFFIX_TEMPLATE = REGISTRATE
            .item("incomplete_crystal_affix_template", SequencedAssemblyItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                    .texture("layer0", prov.modLoc("item/crystal_affix_template")))
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_APOTHEOTIC_AFFIX_TEMPLATE = REGISTRATE
            .item("incomplete_apotheotic_affix_template", SequencedAssemblyItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                    .texture("layer0", prov.modLoc("item/apotheotic_affix_template")))
            .register();

    public static final ItemEntry<AffixTemplateItem> BRASS_AFFIX_TEMPLATE = REGISTRATE
            .item("brass_affix_template", AffixTemplateItem::brass)
            .properties(prop -> prop
                    .rarity(Rarity.COMMON))
            .register();

    public static final ItemEntry<AffixTemplateItem> CRYSTAL_AFFIX_TEMPLATE = REGISTRATE
            .item("crystal_affix_template", AffixTemplateItem::crystal)
            .properties(prop -> prop
                    .rarity(Rarity.UNCOMMON))
            .register();

    public static final ItemEntry<AffixTemplateItem> APOTHEOTIC_AFFIX_TEMPLATE = REGISTRATE
            .item("apotheotic_affix_template", AffixTemplateItem::apotheotic)
            .properties(prop -> prop
                    .rarity(Rarity.RARE))
            .register();

    public static void register() {
        REGISTRATE.registerItemTags(MOD_TAGS);
    }

    public static class ModTags extends ItemTagRegistry {
        public final TagKey<Item> blazeComposerSuperActivators = tag("blaze_composer/super_activators", "Blaze Composer Super Activators");

        public ModTags() {
            super(CEIACommon.ID);
            addOptional(FORGE_BUCKETS, CEICommon.asResource("apotheotic_essence_bucket"));
            addOptional(FORGE_BUCKETS, CEICommon.asResource("crystal_essence_bucket"));
            addOptional(blazeComposerSuperActivators, new ResourceLocation("apotheosis", "mythic_material"));
            addOptionalBlockDrop("gem_cutter");
            addOptionalBlockDrop("affix_augmentor");
            addOptionalBlockDrop("blaze_composer");
        }

        private void addOptionalBlockDrop(String path) {
            ResourceLocation item = CEICommon.asResource(path);
            addOptional(TagKey.create(Registries.ITEM, CEIConditionalLootTables.optionalDropTag(item)), item);
        }
    }
}
