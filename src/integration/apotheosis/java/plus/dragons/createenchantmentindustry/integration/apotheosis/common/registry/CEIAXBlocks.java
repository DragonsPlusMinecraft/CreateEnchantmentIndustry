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

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeMovementBehaviour;
import plus.dragons.createdragonsplus.data.tag.IntrinsicTagRegistry;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance.LowerAssemblyOperatorBlockItem;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer.AffixAugmentorBlock;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerBlock;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerBlockItem;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutterBlock;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

@SuppressWarnings("removal")
public class CEIAXBlocks {
    public static final BlockEntry<GemCutterBlock> GEM_CUTTER = REGISTRATE
            .block("gem_cutter", GemCutterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(LowerAssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<AffixAugmentorBlock> AFFIX_AUGMENTOR = REGISTRATE
            .block("affix_augmentor", AffixAugmentorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(LowerAssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<BlazeComposerBlock> BLAZE_COMPOSER = REGISTRATE
            .block("blaze_composer", BlazeComposerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).lightLevel(BlazeBlock::getLight))
            .addLayer(() -> RenderType::cutoutMipped)
            .onRegister(block -> MovementBehaviour.REGISTRY.register(block, new BlazeMovementBehaviour()))
            .blockstate((ctx, prov) -> prov.horizontalBlock(
                    ctx.getEntry(),
                    prov.models().getExistingFile(Create.asResource("block/blaze_burner/block"))))
            .item(BlazeComposerBlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
                    Create.asResource("block/blaze_burner/block_with_blaze")))
            .build()
            .register();

    public static final ModTags MOD_TAGS = new ModTags();

    public static void register(IEventBus modBus) {
        REGISTRATE.registerBlockTags(MOD_TAGS);
    }

    public static class ModTags extends IntrinsicTagRegistry<Block, RegistrateTagsProvider.IntrinsicImpl<Block>> {
        public final TagKey<Block> fanSalvagingCatalysts = tag("fan_processing_catalysts/salvaging", "Bulk Salvaging Catalysts");

        public ModTags() {
            super(CEIACommon.ID, Registries.BLOCK);
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, GEM_CUTTER.getId());
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, AFFIX_AUGMENTOR.getId());
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, BLAZE_COMPOSER.getId());
            addOptional(AllBlockTags.FAN_TRANSPARENT.tag, BLAZE_COMPOSER.getId());
            addOptional(AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag, BLAZE_COMPOSER.getId());
        }

        @Override
        public void generate(RegistrateTagsProvider.IntrinsicImpl<Block> provider) {
            super.generate(provider);
            provider.addTag(fanSalvagingCatalysts);
        }
    }
}
