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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;

public record InfusionStats(float eterna, float quanta, float arcana) implements IHaveGoggleInformation {

    public static final InfusionStats EMPTY = new InfusionStats(0f, 15f, 0f);
    public static InfusionStats parse(Tag tag) {
        if (!(tag instanceof CompoundTag compound))
            return EMPTY;
        return new InfusionStats(
                clamp(compound.getFloat("Eterna")),
                clamp(compound.getFloat("Quanta")),
                clamp(compound.getFloat("Arcana")));
    }

    public CompoundTag tag() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Eterna", eterna);
        tag.putFloat("Quanta", quanta);
        tag.putFloat("Arcana", arcana);
        return tag;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(eterna);
        buffer.writeFloat(quanta);
        buffer.writeFloat(arcana);
    }

    public static InfusionStats read(FriendlyByteBuf buffer) {
        return new InfusionStats(clamp(buffer.readFloat()), clamp(buffer.readFloat()), clamp(buffer.readFloat()));
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(100f, value));
    }

    public boolean qualified(InfusionStats input) {
        return input.eterna() >= eterna && input.quanta() >= quanta && input.arcana() >= arcana;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEIALang.translate("gui.goggles.apotheotic_stats")
                .forGoggles(tooltip);
        CEIALang.translate("gui.goggles.infuser.stats.eterna", eterna).style(ChatFormatting.GREEN)
                .add(CEIALang.translate("gui.goggles.infuser.stats.arcana", arcana).text("% ").style(ChatFormatting.LIGHT_PURPLE))
                .add(CEIALang.translate("gui.goggles.infuser.stats.quanta", quanta).text("%").style(ChatFormatting.RED))
                .forGoggles(tooltip, 1);
        return true;
    }
}
