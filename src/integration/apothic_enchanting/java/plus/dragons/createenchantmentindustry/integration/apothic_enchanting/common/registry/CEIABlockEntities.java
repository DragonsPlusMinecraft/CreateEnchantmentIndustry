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

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagRenderer;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.brass.BrassBookshelfBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.brass.BrassBookshelfRenderer;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.brass.BrassBookshelfVisual;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf.creative.CreativeBookshelfBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserRenderer;

public class CEIABlockEntities {
    public static final BlockEntityEntry<InfuserBlockEntity> INFUSER = REGISTRATE
            .blockEntity("infuser", InfuserBlockEntity::new)
            .renderer(() -> InfuserRenderer::new)
            .validBlock(CEIABlocks.INFUSER)
            .register();

    public static final BlockEntityEntry<BrassBookshelfBlockEntity> BRASS_BOOKSHELF = REGISTRATE
            .blockEntity("brass_bookshelf", BrassBookshelfBlockEntity::new)
            .visual(() -> BrassBookshelfVisual::new, false)
            .renderer(() -> BrassBookshelfRenderer::new)
            .validBlock(CEIABlocks.BRASS_BOOKSHELF)
            .register();

    public static final BlockEntityEntry<CreativeBookshelfBlockEntity> CREATIVE_BOOKSHELF = REGISTRATE
            .blockEntity("creative_bookshelf", CreativeBookshelfBlockEntity::new)
            .validBlock(CEIABlocks.CREATIVE_BOOKSHELF)
            .register();

    public static final BlockEntityEntry<EnderWovenBagBlockEntity> ENDER_WOVEN_BAG = REGISTRATE
            .blockEntity("ender_woven_bag", EnderWovenBagBlockEntity::new)
            .renderer(() -> EnderWovenBagRenderer::new)
            .validBlock(CEIABlocks.ENDER_WOVEN_BAG)
            .register();

    public static void register(IEventBus modBus) {
        // Forge 1.20 block entities expose capabilities from getCapability directly.
    }
}
