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

package plus.dragons.createenchantmentindustry.common.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** The single, versioned Forge network channel used by CEI. */
public final class CEINetwork {
    public static final String PROTOCOL_VERSION = "1";
    public static final int DATA_MAP_SYNC_PACKET_ID = 0;
    public static final int CONTRAPTION_ENDER_WOVEN_BAG_PACKET_ID = 1;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CEICommon.asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static boolean registered;

    private CEINetwork() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        registerMessage(
                DATA_MAP_SYNC_PACKET_ID,
                CEIDataMapSyncPacket.class,
                CEIDataMapSyncPacket::encode,
                CEIDataMapSyncPacket::decode,
                CEIDataMapSyncPacket::handle,
                NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void registerMessage(
            int id,
            Class<T> type,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler,
            NetworkDirection direction) {
        CHANNEL.registerMessage(id, type, encoder, decoder, handler, java.util.Optional.of(direction));
    }
}
