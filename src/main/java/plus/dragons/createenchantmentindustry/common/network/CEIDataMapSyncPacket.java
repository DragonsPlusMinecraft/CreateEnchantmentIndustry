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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;

public record CEIDataMapSyncPacket(Map<ResourceLocation, Map<ResourceLocation, JsonElement>> values) {
    private static final int MAX_TYPE_COUNT = 64;
    private static final int MAX_ENTRY_COUNT = 65_536;
    private static final int MAX_TOTAL_ENTRY_COUNT = 131_072;
    private static final int MAX_VALUE_JSON_LENGTH = 1_048_576;

    public static CEIDataMapSyncPacket create() {
        return new CEIDataMapSyncPacket(CEIDataMaps.serializeServerSnapshot());
    }

    public static void encode(CEIDataMapSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.values.size());
        packet.values.forEach((type, entries) -> {
            buffer.writeResourceLocation(type);
            buffer.writeVarInt(entries.size());
            entries.forEach((key, value) -> {
                buffer.writeResourceLocation(key);
                buffer.writeUtf(value.toString(), MAX_VALUE_JSON_LENGTH);
            });
        });
    }

    public static CEIDataMapSyncPacket decode(FriendlyByteBuf buffer) {
        int typeCount = readBoundedCount(buffer, MAX_TYPE_COUNT, "type");
        int totalEntryCount = 0;
        Map<ResourceLocation, Map<ResourceLocation, JsonElement>> values = new LinkedHashMap<>();
        for (int typeIndex = 0; typeIndex < typeCount; typeIndex++) {
            ResourceLocation type = buffer.readResourceLocation();
            int entryCount = readBoundedCount(buffer, MAX_ENTRY_COUNT, "entry");
            totalEntryCount += entryCount;
            if (totalEntryCount > MAX_TOTAL_ENTRY_COUNT) {
                throw new IllegalArgumentException("CEI data-map snapshot contains too many total entries: " + totalEntryCount);
            }
            Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();
            for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
                ResourceLocation key = buffer.readResourceLocation();
                JsonElement previous = entries.put(key, JsonParser.parseString(buffer.readUtf(MAX_VALUE_JSON_LENGTH)));
                if (previous != null) {
                    throw new IllegalArgumentException("CEI data-map snapshot contains duplicate key " + key + " in " + type);
                }
            }
            if (values.put(type, Map.copyOf(entries)) != null) {
                throw new IllegalArgumentException("CEI data-map snapshot contains duplicate type " + type);
            }
        }
        return new CEIDataMapSyncPacket(Map.copyOf(values));
    }

    private static int readBoundedCount(FriendlyByteBuf buffer, int maximum, String description) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException(
                    "CEI data-map snapshot " + description + " count " + count + " exceeds limit " + maximum);
        }
        return count;
    }

    public static void handle(CEIDataMapSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> CEIDataMaps.applyClientSnapshot(packet.values));
        context.setPacketHandled(true);
    }
}
