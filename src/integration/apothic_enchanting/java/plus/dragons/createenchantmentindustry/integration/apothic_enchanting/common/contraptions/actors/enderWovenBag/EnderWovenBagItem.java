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

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.util.CEIALang;

public class EnderWovenBagItem extends BlockItem {
    public EnderWovenBagItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        StoredEntities entities = readEntities(stack);
        if (level == null || entities.count() == 0) {
            return;
        }
        entities.getEntityNames(level).forEach((key, value) -> {
            var builder = CEIALang.builder().add(key.copy());
            if (value > 1)
                builder.add(CEIALang.text(" x" + value).style(ChatFormatting.GRAY).component());
            builder.addTo(tooltipComponents);
        });
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        StoredEntities stored = readEntities(context.getItemInHand());
        InteractionResult result = super.place(context);
        if (result.consumesAction()
                && !context.getLevel().isClientSide
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof EnderWovenBagBlockEntity bag) {
            bag.setStoredEntities(stored);
        }
        return result;
    }

    @OnlyIn(Dist.CLIENT)
    public static float override(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity livingEntity, int seed) {
        StoredEntities entities = readEntities(stack);
        if (entities.full()) return 1;
        else return 0;
    }

    private static StoredEntities readEntities(ItemStack stack) {
        var owned = CEIItemData.getOwnedData(stack, CEIItemData.STORED_ENTITIES_TAG);
        if (owned != null) {
            return StoredEntities.parse(owned);
        }
        // Pre-schema 1.20 and vanilla BlockEntityTag compatibility.
        var blockEntityTag = BlockItem.getBlockEntityData(stack);
        return blockEntityTag == null ? new StoredEntities() : StoredEntities.parse(blockEntityTag.get("Entities"));
    }
}
