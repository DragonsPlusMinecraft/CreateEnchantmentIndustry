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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.contraptions.actors.enderWovenBag.EnderWovenBagClientPacketHandler;

public record ContraptionEnderWovenBagPocketChangePacket(int entityId, BlockPos localPos, boolean open) {
    public static void encode(ContraptionEnderWovenBagPocketChangePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBlockPos(packet.localPos);
        buffer.writeBoolean(packet.open);
    }

    public static ContraptionEnderWovenBagPocketChangePacket decode(FriendlyByteBuf buffer) {
        return new ContraptionEnderWovenBagPocketChangePacket(
                buffer.readVarInt(), buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(
            ContraptionEnderWovenBagPocketChangePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> EnderWovenBagClientPacketHandler.handle(packet)));
        context.setPacketHandled(true);
    }
}
