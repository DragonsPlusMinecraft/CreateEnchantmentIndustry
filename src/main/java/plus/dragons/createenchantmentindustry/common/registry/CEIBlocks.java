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

package plus.dragons.createenchantmentindustry.common.registry;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.materials.ExperienceBlock;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlock;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlock;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@SuppressWarnings("removal")
public class CEIBlocks {
    public static final BlockEntry<MechanicalGrindstoneBlock> MECHANICAL_GRINDSTONE = REGISTRATE
            .block("mechanical_grindstone", MechanicalGrindstoneBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(CEIConfig.stress().setImpact(4.0))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .item(MechanicalGrindStoneItem::new)
            .build()
            .register();
    public static final BlockEntry<GrindstoneDrainBlock> GRINDSTONE_DRAIN = REGISTRATE
            .block("grindstone_drain", prop -> new GrindstoneDrainBlock(MECHANICAL_GRINDSTONE.get(), prop))
            .initialProperties(SharedProperties::copperMetal)
            .transform(CEIConfig.stress().setImpact(4.0))
            .transform(pickaxeOnly())
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel())
            .loot((loots, block) -> loots.add(block, LootTable.lootTable()
                    .withPool(loots.applyExplosionCondition(MECHANICAL_GRINDSTONE, LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(MECHANICAL_GRINDSTONE))))
                    .withPool(loots.applyExplosionCondition(AllBlocks.ITEM_DRAIN, LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(AllBlocks.ITEM_DRAIN))))))
            .register();
    public static final BlockEntry<ExperienceHatchBlock> EXPERIENCE_HATCH = REGISTRATE
            .block("experience_hatch", ExperienceHatchBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GREEN).lightLevel(state -> 12))
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.get(), AssetLookup.standardModel(ctx, prov)))
            .simpleItem()
            .register();
    public static final BlockEntry<PrinterBlock> PRINTER = REGISTRATE
            .block("printer", PrinterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();
    public static final BlockEntry<BlazeEnchanterBlock> BLAZE_ENCHANTER = REGISTRATE
            .block("blaze_enchanter", BlazeEnchanterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).lightLevel(BlazeBlock::getLight))
            .transform(pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag)
            .blockstate((ctx, prov) -> prov.horizontalBlock(
                    ctx.getEntry(),
                    prov.models().getExistingFile(Create.asResource("block/blaze_burner/block"))))
            .item()
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
                    Create.asResource("block/blaze_burner/block_with_blaze")))
            .build()
            .register();
    public static final BlockEntry<BlazeForgerBlock> BLAZE_FORGER = REGISTRATE
            .block("blaze_forger", BlazeForgerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).lightLevel(BlazeBlock::getLight))
            .transform(pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag)
            .blockstate((ctx, prov) -> prov.horizontalBlock(
                    ctx.getEntry(),
                    prov.models().getExistingFile(Create.asResource("block/blaze_burner/block"))))
            .item()
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
                    Create.asResource("block/blaze_burner/block_with_blaze")))
            .build()
            .register();
    public static final BlockEntry<ExperienceBlock> SUPER_EXPERIENCE_BLOCK = REGISTRATE
            .block("super_experience_block", ExperienceBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.DIAMOND)
                    .sound(new DeferredSoundType(1, .5f, () -> SoundEvents.AMETHYST_BLOCK_BREAK,
                            () -> SoundEvents.AMETHYST_BLOCK_STEP, () -> SoundEvents.AMETHYST_BLOCK_PLACE,
                            () -> SoundEvents.AMETHYST_BLOCK_HIT, () -> SoundEvents.AMETHYST_BLOCK_FALL))
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
                    .withExistingParent(ctx.getName(), Create.asResource("block/experience_block"))
                    .texture("all", ctx.getId().withPrefix("block/"))
                    .texture("particle", ctx.getId().withPrefix("block/"))))
            .transform(pickaxeOnly())
            .lang("Block of Super Experience")
            .tag(Tags.Blocks.STORAGE_BLOCKS)
            .tag(BlockTags.BEACON_BASE_BLOCKS)
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(Tags.Items.STORAGE_BLOCKS)
            .build()
            .register();

    public static void register(IEventBus modBus) {}
}
