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

import net.minecraftforge.network.NetworkDirection;
import plus.dragons.createenchantmentindustry.common.network.CEINetwork;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.ContraptionEnderWovenBagPocketChangePacket;

public final class CEIAPackets {
    private static boolean registered;

    private CEIAPackets() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CEINetwork.registerMessage(
                CEINetwork.CONTRAPTION_ENDER_WOVEN_BAG_PACKET_ID,
                ContraptionEnderWovenBagPocketChangePacket.class,
                ContraptionEnderWovenBagPocketChangePacket::encode,
                ContraptionEnderWovenBagPocketChangePacket::decode,
                ContraptionEnderWovenBagPocketChangePacket::handle,
                NetworkDirection.PLAY_TO_CLIENT);
    }
}
