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

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockVisual;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainRenderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterVisual;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerRenderer;

public class CEIBlockEntities {
    public static final BlockEntityEntry<KineticBlockEntity> MECHANICAL_GRINDSTONE = REGISTRATE
            .blockEntity("mechanical_grindstone", KineticBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(CEIPartialModels.MECHANICAL_GRINDSTONE), false)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .validBlock(CEIBlocks.MECHANICAL_GRINDSTONE)
            .register();
    public static final BlockEntityEntry<GrindstoneDrainBlockEntity> GRINDSTONE_DRAIN = REGISTRATE
            .blockEntity("grindstone_drain", GrindstoneDrainBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(CEIPartialModels.MECHANICAL_GRINDSTONE), true)
            .renderer(() -> GrindstoneDrainRenderer::new)
            .validBlock(CEIBlocks.GRINDSTONE_DRAIN)
            .register();
    public static final BlockEntityEntry<ExperienceHatchBlockEntity> EXPERIENCE_HATCH = REGISTRATE
            .blockEntity("experience_hatch", ExperienceHatchBlockEntity::new)
            .renderer(() -> SmartBlockEntityRenderer::new)
            .validBlock(CEIBlocks.EXPERIENCE_HATCH)
            .register();
    public static final BlockEntityEntry<PrinterBlockEntity> PRINTER = REGISTRATE
            .blockEntity("printer", PrinterBlockEntity::new)
            .renderer(() -> PrinterRenderer::new)
            .validBlock(CEIBlocks.PRINTER)
            .register();
    public static final BlockEntityEntry<BlazeEnchanterBlockEntity> BLAZE_ENCHANTER = REGISTRATE
            .blockEntity("blaze_enchanter", BlazeEnchanterBlockEntity::new)
            .visual(() -> BlazeBlockVisual::new)
            .renderer(() -> BlazeEnchanterRenderer::new)
            .validBlock(CEIBlocks.BLAZE_ENCHANTER)
            .register();
    public static final BlockEntityEntry<BlazeForgerBlockEntity> BLAZE_FORGER = REGISTRATE
            .blockEntity("blaze_forger", BlazeForgerBlockEntity::new)
            .visual(() -> BlazeBlockVisual::new)
            .renderer(() -> BlazeForgerRenderer::new)
            .validBlock(CEIBlocks.BLAZE_FORGER)
            .register();
    public static final BlockEntityEntry<ClassicBlazeEnchanterBlockEntity> CLASSIC_BLAZE_ENCHANTER = REGISTRATE
            .blockEntity("classic_blaze_enchanter", ClassicBlazeEnchanterBlockEntity::new)
            .visual(() -> ClassicBlazeEnchanterVisual::new)
            .renderer(() -> ClassicBlazeEnchanterRenderer::new)
            .validBlock(CEIBlocks.CLASSIC_BLAZE_ENCHANTER)
            .register();
    public static final BlockEntityEntry<ExperienceLanternBlockEntity> EXPERIENCE_LANTERN = REGISTRATE
            .blockEntity("experience_lantern", ExperienceLanternBlockEntity::new)
            .validBlock(CEIBlocks.EXPERIENCE_LANTERN)
            .register();

    public static void register(IEventBus modBus) {
        // Forge 1.20.1 block entities expose capabilities from getCapability directly.
    }
}
