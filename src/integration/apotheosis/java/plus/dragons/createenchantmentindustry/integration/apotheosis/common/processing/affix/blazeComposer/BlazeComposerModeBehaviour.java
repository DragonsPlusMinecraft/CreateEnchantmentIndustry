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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer;

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

public class BlazeComposerModeBehaviour extends ScrollValueBehaviour {
    public static final BehaviourType<BlazeComposerModeBehaviour> TYPE = new BehaviourType<>("blaze_composer_mode");

    private final BlazeComposerBlockEntity composer;

    public BlazeComposerModeBehaviour(BlazeComposerBlockEntity composer, ValueBoxTransform transform) {
        super(CEILang.translate("gui.blaze_composer.mode_selector").component(), composer, transform);
        this.composer = composer;
        this.between(0, BlazeComposerMode.values().length - 1);
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
        nbt.putInt("BlazeComposerMode", getValue());
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        if (nbt.contains("BlazeComposerMode"))
            composer.mode = BlazeComposerMode.BY_ID.apply(nbt.getInt("BlazeComposerMode"));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
                label,
                0,
                BlazeComposerMode.values().length,
                Arrays.stream(BlazeComposerMode.values())
                        .map(BlazeComposerModeBehaviour::modeName)
                        .map(Component.class::cast)
                        .toList(),
                new ValueSettingsFormatter(valueSettings -> modeName(BlazeComposerMode.BY_ID.apply(valueSettings.row()))));
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
        value = Mth.clamp(value, 0, BlazeComposerMode.values().length - 1);
        composer.setMode(BlazeComposerMode.BY_ID.apply(value));
    }

    @Override
    public int getValue() {
        return composer.getMode().ordinal();
    }

    @Override
    public String formatValue() {
        return modeName(composer.getMode()).getString();
    }

    private static MutableComponent modeName(BlazeComposerMode mode) {
        return Component.translatable("create_enchantment_industry.gui.blaze_composer.mode." + mode.getSerializedName());
    }
}
