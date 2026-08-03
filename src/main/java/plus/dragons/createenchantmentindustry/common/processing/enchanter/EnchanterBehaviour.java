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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
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
        enchanting.update(getWorld(), stack, value, enchanter.special, enchanter.cursed, enchanter.getRandom());
    }

    public ItemStack getResult(ItemStack stack) {
        return enchanting.getResult(getWorld(), stack, enchanter.getRandom(), enchanter.special);
    }

    public int getExperienceCost() {
        return enchanting.getExperienceCost(enchanter.special, isTemplateMode());
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean isTemplateMode() {
        return !template.isEmpty();
    }

    public boolean setTemplate(ItemStack stack) {
        if (!loadTemplate(stack))
            return false;
        update(enchanter.heldItem);
        if (!(getWorld().isClientSide)) {
            blockEntity.setChanged();
            blockEntity.sendData();
        }
        return true;
    }

    private boolean loadTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            template = ItemStack.EMPTY;
            enchanting = new EnchantingBehaviour();
        } else if (stack.getItem() instanceof EnchantingTemplateItem) {
            template = stack;
            enchanting = new TemplateEnchantingBehaviour(template);
        } else return false;
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
        ItemStack template = stack.copy();
        template.setCount(1);
        if (!setTemplate(template)) {
            player.displayClientMessage(CEILang.translate("gui.blaze_enchanter.template.invalid").component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putInt(LEVEL, value);
        nbt.put(TEMPLATE, template.save(new CompoundTag()));
    }

    @Override
    public void writeSafe(CompoundTag nbt) {
        nbt.putInt(LEVEL, value);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        value = Mth.clamp(nbt.getInt(LEVEL), 0, enchanter.getMaxEnchantLevel());
        loadTemplate(ItemStack.of(nbt.getCompound(TEMPLATE)));
    }

    @Override
    public void initialize() {
        loadTemplate(template);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = true;
        var style = enchanter.special
                ? (enchanter.cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        CEILang.translate(
                "gui.goggles.enchanting.blaze_mode",
                CEILang.translate("gui.blaze_enchanter.blaze_mode." + (enchanter.special ? "super" : "normal")).style(style))
                .forGoggles(tooltip);
        CEILang.translate(
                "gui.goggles.enchanting.mode",
                CEILang.translate("gui.blaze_enchanter.mode." + (isTemplateMode() ? "template" : "direct")).style(ChatFormatting.AQUA))
                .forGoggles(tooltip);
        if (!template.isEmpty()) {
            CEILang.translate("gui.goggles.enchanting.template").forGoggles(tooltip);
            CEILang.item(template).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        }
        if (value > 0) {
            CEILang.translate("gui.goggles.enchanting.level", CEILang.number(value).style(style))
                    .forGoggles(tooltip);
        } else {
            CEILang.translate("gui.goggles.enchanting.level.not_set").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        if (enchanter.special && enchanter.cursed) {
            CEILang.translate("gui.goggles.enchanting.blocked_super_penalty")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
        }
        if (enchanter.heldItem.isEmpty()) {
            addModeHelp(tooltip);
            return true;
        }
        if (enchanter.processingTime == -1
                && (!CEIItemData.getEnchantments(enchanter.heldItem).isEmpty()
                        || !CEIItemData.getStoredEnchantments(enchanter.heldItem).isEmpty())) {
            CEILang.translate("gui.goggles.enchanting.completed").style(ChatFormatting.GREEN).forGoggles(tooltip);
            return true;
        }
        boolean canProcess = canProcess(enchanter.heldItem);
        int cost = canProcess ? getExperienceCost() : 0;
        if (canProcess && cost > 0) {
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            CEILang.translate("gui.goggles.enchanting.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            addAvailableEnchantments(tooltip, isPlayerSneaking);
            int experience = enchanter.special ? enchanter.getSpecialExperience() : enchanter.getTotalExperience();
            if (experience < cost) {
                CEILang.translate(
                        enchanter.special ? "gui.goggles.enchanting.insufficient_super_experience" : "gui.goggles.enchanting.insufficient_experience",
                        CEILang.number(experience).add(mb).style(style),
                        CEILang.number(cost).add(mb).style(style))
                        .style(ChatFormatting.RED)
                        .forGoggles(tooltip);
            }
        } else {
            CEILang.translate("gui.goggles.enchanting.invalid_item").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        return added;
    }

    private void addModeHelp(List<Component> tooltip) {
        String mode = isTemplateMode() ? "template" : "direct";
        CEILang.translate("gui.goggles.enchanting.mode_help." + mode)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.translate("gui.goggles.enchanting.requires").forGoggles(tooltip);
        CEILang.translate("gui.goggles.enchanting.requires." + mode)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private void addAvailableEnchantments(List<Component> tooltip, boolean isPlayerSneaking) {
        List<EnchantmentInstance> available = enchanting.getPreviewEnchantments();
        if (available.isEmpty())
            return;
        CEILang.translate("gui.goggles.enchanting.available_targets").forGoggles(tooltip);
        int limit = isPlayerSneaking ? available.size() : Math.min(available.size(), 6);
        for (int i = 0; i < limit; i++) {
            EnchantmentInstance instance = available.get(i);
            var name = instance.enchantment.getFullname(instance.level).copy();
            if (instance.enchantment.isCurse()) {
                name.append(" ?");
            }
            ChatFormatting style = instance.enchantment.isCurse()
                    ? ChatFormatting.RED
                    : ChatFormatting.GRAY;
            CEILang.builder().add(name).style(style).forGoggles(tooltip, 1);
        }
        if (!isPlayerSneaking && available.size() > limit) {
            CEILang.translate("gui.goggles.enchanting.available_targets.more", available.size() - limit)
                    .style(ChatFormatting.DARK_GRAY)
                    .forGoggles(tooltip, 1);
        }
    }
}
