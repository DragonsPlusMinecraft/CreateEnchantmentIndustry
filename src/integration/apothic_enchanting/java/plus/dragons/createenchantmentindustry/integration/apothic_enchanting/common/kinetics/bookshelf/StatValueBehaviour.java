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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.kinetics.bookshelf;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;

public class StatValueBehaviour extends ScrollValueBehaviour {
    public static final BehaviourType<StatValueBehaviour> TYPE = new BehaviourType<>("stat_value");
    protected IHaveStatType statType;
    private int eterna;
    private int quanta;
    private int arcana;
    private boolean hasNegative;
    private Function<EnchantmentStatType, Integer> maxFunction;

    public StatValueBehaviour(Component label, IHaveStatType statType, SmartBlockEntity be, ValueBoxTransform slot, Function<EnchantmentStatType, Integer> maxFunction, boolean hasNegative) {
        super(label, be, slot);
        this.statType = statType;
        this.eterna = 0;
        this.quanta = 0;
        this.arcana = 0;
        this.maxFunction = maxFunction;
        this.hasNegative = hasNegative;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        var statType = this.statType.getStatType();
        int max = maxFunction.apply(statType);
        return new ValueSettingsBoard(
                label,
                max,
                max / 5,
                hasNegative ? ImmutableList.of(Component.literal("-").append(Component.translatable(statType.getTranslationKey())), Component.literal("+").append(Component.translatable(statType.getTranslationKey()))) : ImmutableList.of(Component.translatable(statType.getTranslationKey())),
                new ValueSettingsFormatter(valueSettings -> {
                    if (statType == EnchantmentStatType.ETERNA)
                        return CEIALang.number(valueSettings.value()).text(".00").component();
                    else return CEIALang.number(valueSettings.value()).text(".00%").component();
                }));
    }

    @Override
    public boolean isSafeNBT() {
        return false;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putInt("StatValueEterna", this.eterna);
        nbt.putInt("StatValueQuanta", this.quanta);
        nbt.putInt("StatValueArcana", this.arcana);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        if (nbt.contains("StatValue")) return; // prevent crash from loading 0.1.0 version
        quanta = nbt.getInt("StatValueQuanta");
        arcana = nbt.getInt("StatValueArcana");
        eterna = nbt.getInt("StatValueEterna");
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, getValue());
    }

    @Override
    public int netId() {
        return 2;
    }

    @Override
    public void setValue(int value) {
        var max = maxFunction.apply(statType.getStatType());
        value = Mth.clamp(value, hasNegative ? -max : 0, max);
        if (value == getValue())
            return;
        var statType = this.statType.getStatType();
        if (statType == EnchantmentStatType.ETERNA) eterna = value;
        else if (statType == EnchantmentStatType.QUANTA) quanta = value;
        else arcana = value;
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (valueSetting.equals(getValueSettings()))
            return;
        setValue(hasNegative ? (valueSetting.row() == 0 ? -valueSetting.value() : valueSetting.value()) : valueSetting.value());
        playFeedbackSound(this);
    }

    @Override
    public int getValue() {
        var statType = this.statType.getStatType();
        return statType == EnchantmentStatType.ETERNA ? eterna : (statType == EnchantmentStatType.QUANTA ? quanta : arcana);
    }

    public int getValue(EnchantmentStatType type) {
        return type == EnchantmentStatType.ETERNA ? eterna : (type == EnchantmentStatType.QUANTA ? quanta : arcana);
    }

    @Override
    public String formatValue() {
        var statType = this.statType.getStatType();
        return (value > 0 ? "+" : "") + (statType == EnchantmentStatType.ETERNA ? eterna + ".00" : (statType == EnchantmentStatType.QUANTA ? quanta + ".00%" : arcana + ".00%"));
    }
}
