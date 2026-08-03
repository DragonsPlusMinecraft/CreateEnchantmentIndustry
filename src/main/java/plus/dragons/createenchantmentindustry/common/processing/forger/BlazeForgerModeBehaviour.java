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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class BlazeForgerModeBehaviour extends ScrollValueBehaviour {
    public static final BehaviourType<BlazeForgerModeBehaviour> TYPE = new BehaviourType<>("blaze_forger_mode");
    public static final String MODE = "BlazeForgerMode";

    private final BlazeForgerBlockEntity forger;

    public BlazeForgerModeBehaviour(BlazeForgerBlockEntity forger, ValueBoxTransform transform) {
        super(CEILang.translate("gui.blaze_forger.mode_selector").component(), forger, transform);
        this.forger = forger;
        this.between(0, BlazeForgerMode.values().length - 1);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isSafeNBT() {
        return false;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putInt(MODE, getValue());
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        if (nbt.contains(MODE))
            forger.mode = BlazeForgerMode.BY_ID.apply(nbt.getInt(MODE));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
                label,
                0,
                BlazeForgerMode.values().length,
                Arrays.stream(BlazeForgerMode.values())
                        .map(BlazeForgerModeBehaviour::modeName)
                        .map(Component.class::cast)
                        .toList(),
                new ValueSettingsFormatter(valueSettings -> modeName(BlazeForgerMode.BY_ID.apply(valueSettings.row()))));
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (valueSetting.equals(getValueSettings()))
            return;
        setValue(valueSetting.row());
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(getValue(), 0);
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, 0, BlazeForgerMode.values().length - 1);
        forger.setMode(BlazeForgerMode.BY_ID.apply(value));
    }

    @Override
    public int getValue() {
        return forger.getMode().ordinal();
    }

    @Override
    public String formatValue() {
        return modeName(forger.getMode()).getString();
    }

    private static MutableComponent modeName(BlazeForgerMode mode) {
        return Component.translatable("create_enchantment_industry.gui.blaze_forger.mode." + mode.getSerializedName());
    }
}
