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

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class StatTypeBehaviour extends ScrollOptionBehaviour<EnchantmentStatType> {
    public static final BehaviourType<StatTypeBehaviour> TYPE = new BehaviourType<>("stat_type");

    public StatTypeBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(EnchantmentStatType.class, label, be, slot);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putInt("StatType", value);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        value = nbt.getInt("StatType");
    }
}
