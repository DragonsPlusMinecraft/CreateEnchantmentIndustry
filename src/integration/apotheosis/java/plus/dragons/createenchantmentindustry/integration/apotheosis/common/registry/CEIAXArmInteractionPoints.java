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

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerArmInteractionPoint;

public class CEIAXArmInteractionPoints {
    public static final RegistryEntry<BlazeComposerArmInteractionPoint.Type> BLAZE_COMPOSER = REGISTRATE
            .armInteractionPoint("blaze_composer", BlazeComposerArmInteractionPoint.Type::new)
            .register();

    public static void register(IEventBus modBus) {}
}
