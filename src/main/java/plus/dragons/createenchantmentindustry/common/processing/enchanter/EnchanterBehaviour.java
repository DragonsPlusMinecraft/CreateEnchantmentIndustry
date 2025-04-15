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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import java.util.List;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour.EnchantingBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour.TemplateEnchantingBehaviour;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class EnchanterBehaviour extends ScrollValueBehaviour implements IHaveGoggleInformation {
    public static final BehaviourType<EnchanterBehaviour> TYPE = new BehaviourType<>();
    public static final String LEVEL = "EnchantingLevel";
    public static final String TEMPLATE = "EnchantingTemplate";
    private final BlazeEnchanterBlockEntity enchanter;
    private ItemStack template = ItemStack.EMPTY;
    private EnchantingBehaviour enchanting = new EnchantingBehaviour();
    ValueBoxTransform.Sided templateItemTransform;

    public EnchanterBehaviour(BlazeEnchanterBlockEntity enchanter, ValueBoxTransform transform, ValueBoxTransform.Sided templateItemTransform) {
        super(CEILang.translate("gui.blaze_enchanter.level").component(), enchanter, transform);
        this.enchanter = enchanter;
        this.templateItemTransform = templateItemTransform;
    }

    public ValueBoxTransform getTemplateItemSlotPositioning() {
        return templateItemTransform;
    }

    public boolean canProcess(ItemStack stack) {
        return enchanting.canProcess(getWorld(), stack, enchanter.special);
    }

    public float getRenderDistance() {
        return AllConfigs.client().filterItemRenderDistance.getF();
    }

    public void update(ItemStack stack) {
        enchanting.update(getWorld(), stack, value, enchanter.special, enchanter.cursed);
    }

    public ItemStack getResult(ItemStack stack) {
        return enchanting.getResult(getWorld(), stack, enchanter.getRandom(), enchanter.special);
    }

    public int getExperienceCost() {
        return enchanting.getExperienceCost();
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean setTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            template = ItemStack.EMPTY;
            enchanting = new EnchantingBehaviour();
        } else if (stack.isEnchantable()) {
            template = stack;
            enchanting = new TemplateEnchantingBehaviour(template);
        } else return false;
        update(enchanter.heldItem);
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, 0, enchanter.getMaxEnchantLevel());
        if (value == this.value)
            return;
        this.value = value;
        update(enchanter.heldItem);
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        int max = enchanter.getMaxEnchantLevel();
        return new ValueSettingsBoard(
                label,
                max,
                max / 6,
                ImmutableList.of(label),
                new ValueSettingsFormatter(ValueSettings::format));
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);
        if (AllItems.WRENCH.isIn(stack))
            return;
        if (AllBlocks.MECHANICAL_ARM.isIn(stack))
            return;
        var level = getWorld();
        var pos = getPos();
        if (stack.isEmpty()) {
            setTemplate(ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, .25f, .1f);
            return;
        }
        if (!setTemplate(stack.copyWithCount(1))) {
            player.displayClientMessage(CEILang.translate("gui.blaze_enchanter.template.invalid").component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public void write(CompoundTag nbt, Provider registries, boolean clientPacket) {
        nbt.putInt(LEVEL, value);
        nbt.put(TEMPLATE, template.saveOptional(registries));
    }

    @Override
    public void writeSafe(CompoundTag nbt, Provider registries) {
        nbt.putInt(LEVEL, value);
    }

    @Override
    public void read(CompoundTag nbt, Provider registries, boolean clientPacket) {
        value = Math.clamp(nbt.getInt(LEVEL), 0, enchanter.getMaxEnchantLevel());
        template = ItemStack.parseOptional(registries, nbt.getCompound(TEMPLATE));
        var level = getWorld();
        if (level != null)
            setTemplate(template);
    }

    @Override
    public void initialize() {
        setTemplate(template);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = false;
        if (!template.isEmpty()) {
            CEILang.translate("gui.goggles.enchanting.template").forGoggles(tooltip);
            CEILang.item(template).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            added = true;
        }
        var style = enchanter.special
                ? (enchanter.cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        if (value > 0) {
            CEILang.translate("gui.goggles.enchanting.level", CEILang.number(value).style(style))
                    .forGoggles(tooltip);
            added = true;
        }
        int cost = getExperienceCost();
        if (cost > 0) {
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            CEILang.translate("gui.goggles.enchanting.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            added = true;
        }
        return added;
    }
}
