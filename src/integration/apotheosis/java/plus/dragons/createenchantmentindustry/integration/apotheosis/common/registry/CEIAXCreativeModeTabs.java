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

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIACreativeModeTabs;

public class CEIAXCreativeModeTabs {
    public static void register(IEventBus modBus) {
        modBus.addListener(CEIAXCreativeModeTabs::buildContents);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (Apotheosis.enableAdventure && event.getTabKey() == CEIACreativeModeTabs.APOTHEOTIC.getKey()) {
            event.accept(CEIAXBlocks.GEM_CUTTER.get());
            event.accept(CEIAXBlocks.AFFIX_AUGMENTOR.get());
            event.accept(CEIAXBlocks.BLAZE_COMPOSER.get());
            event.accept(CEIAXItems.BRASS_AFFIX_TEMPLATE.get());
            event.accept(CEIAXItems.CRYSTAL_AFFIX_TEMPLATE.get());
            event.accept(CEIAXItems.APOTHEOTIC_AFFIX_TEMPLATE.get());
            event.accept(CEIAXFluids.APOTHEOTIC_ESSENCE.getBucket().get());
            event.accept(CEIAXFluids.CRYSTAL_ESSENCE.getBucket().get());
        }
    }
}
