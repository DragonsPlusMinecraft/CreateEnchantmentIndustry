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

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.data.tag.IntrinsicTagRegistry;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagBlock;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagItem;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagMovementBehaviour;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.brass.BrassBookshelfBlock;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.creative.CreativeBookshelfBlock;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlock;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;

public class CEIABlocks {
    public static final BlockEntry<InfuserBlock> INFUSER = REGISTRATE
            .block("infuser", InfuserBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<BrassBookshelfBlock> BRASS_BOOKSHELF = REGISTRATE
            .block("brass_bookshelf", BrassBookshelfBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(CEIAConfig.server().stress().setImpact(4.0))
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
                    .getExistingFile(ctx.getId())))
            .simpleItem()
            .register();

    public static final BlockEntry<CreativeBookshelfBlock> CREATIVE_BOOKSHELF = REGISTRATE
            .block("creative_bookshelf", CreativeBookshelfBlock::new)
            .initialProperties(SharedProperties::netheriteMetal)
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
                    .getExistingFile(ctx.getId())))
            .item()
            .properties(properties -> properties.rarity(Rarity.EPIC))
            .build()
            .register();

    public static final BlockEntry<EnderWovenBagBlock> ENDER_WOVEN_BAG = REGISTRATE
            .block("ender_woven_bag", EnderWovenBagBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .loot((lt, block) -> {
                lt.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                        .when(ExplosionCondition.survivesExplosion())
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(block)
                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy(
                                                "Entities",
                                                CEIItemData.ROOT_TAG + "." + CEIItemData.STORED_ENTITIES_TAG)))));
            })
            .onRegister(block -> MovementBehaviour.REGISTRY.register(block, new EnderWovenBagMovementBehaviour()))
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(EnderWovenBagItem::new)
            .transform(b -> b.model(AssetLookup.existingItemModel()))
            .build()
            .register();

    public static final ModTags MOD_TAGS = new ModTags();

    public static void register(IEventBus modBus) {
        REGISTRATE.registerBlockTags(MOD_TAGS);
    }

    public static class ModTags extends IntrinsicTagRegistry<Block, RegistrateTagsProvider.IntrinsicImpl<Block>> {
        public ModTags() {
            super(CEIACommon.ID, Registries.BLOCK);
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, INFUSER.getId());
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, BRASS_BOOKSHELF.getId());
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, CREATIVE_BOOKSHELF.getId());
            addOptional(BlockTags.MINEABLE_WITH_PICKAXE, ENDER_WOVEN_BAG.getId());
        }
    }
}
