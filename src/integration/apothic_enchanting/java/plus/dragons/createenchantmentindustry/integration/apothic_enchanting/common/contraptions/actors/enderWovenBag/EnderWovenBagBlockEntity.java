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

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;

public class EnderWovenBagBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private CaptureEntityBehaviour entities;
    public float pocketOpen;

    public EnderWovenBagBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        entities = new CaptureEntityBehaviour(this);
        behaviours.add(entities);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && !isVirtual()) {
            if (entities.full() && pocketOpen < 1) {
                pocketOpen += 0.05;
            } else if (!entities.full() && pocketOpen > 0) {
                pocketOpen -= 0.05;
            }
        }
    }

    public CaptureEntityBehaviour getEntities() {
        return entities;
    }

    public void release(boolean release) {
        entities.release = release;
        setChanged();
        sendData();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEIALang.translate("gui.goggles.captured_mob")
                .forGoggles(tooltip);
        return entities.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    public StoredEntities getStoredEntities() {
        return entities.entities.copy();
    }

    public void setStoredEntities(StoredEntities storedEntities) {
        entities.entities = storedEntities.copy();
        setChanged();
        sendData();
    }

    @Override
    public void saveToItem(ItemStack stack) {
        CEIItemData.setOwnedData(stack, CEIItemData.STORED_ENTITIES_TAG, entities.entities.tag());
    }
}
