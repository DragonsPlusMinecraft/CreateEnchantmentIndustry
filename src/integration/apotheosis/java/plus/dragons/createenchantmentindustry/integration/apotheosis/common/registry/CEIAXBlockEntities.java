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

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockVisual;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer.AffixAugmentorBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer.AffixAugmentorRenderer;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerRenderer;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutterBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutterRenderer;

public class CEIAXBlockEntities {
    public static final BlockEntityEntry<GemCutterBlockEntity> GEM_CUTTER = REGISTRATE
            .blockEntity("gem_cutter", GemCutterBlockEntity::new)
            .renderer(() -> GemCutterRenderer::new)
            .validBlock(CEIAXBlocks.GEM_CUTTER)
            .register();

    public static final BlockEntityEntry<AffixAugmentorBlockEntity> AFFIX_AUGMENTOR = REGISTRATE
            .blockEntity("affix_augmentor", AffixAugmentorBlockEntity::new)
            .renderer(() -> AffixAugmentorRenderer::new)
            .validBlock(CEIAXBlocks.AFFIX_AUGMENTOR)
            .register();

    public static final BlockEntityEntry<BlazeComposerBlockEntity> BLAZE_COMPOSER = REGISTRATE
            .blockEntity("blaze_composer", BlazeComposerBlockEntity::new)
            .visual(() -> BlazeBlockVisual::new)
            .renderer(() -> BlazeComposerRenderer::new)
            .validBlock(CEIAXBlocks.BLAZE_COMPOSER)
            .register();

    public static void register(IEventBus modBus) {}
}
