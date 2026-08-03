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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.util.CodeReference;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.RecipePrintingBehaviour;

public class PrinterBehaviour extends FilteringBehaviour {
    public static final BehaviourType<PrinterBehaviour> TYPE = new BehaviourType<>();
    public static final String TEMPLATE = "PrintingTemplate";
    private final SmartFluidTankBehaviour tank;
    private PrintingBehaviour printing = new RecipePrintingBehaviour(ItemStack.EMPTY);

    public PrinterBehaviour(SmartBlockEntity be, SmartFluidTankBehaviour tank, ValueBoxTransform slot) {
        super(be, slot);
        this.tank = tank;
    }

    public PrintingBehaviour getPrintingBehaviour() {
        return printing;
    }

    public boolean setFilter(ItemStack stack, @Nullable Player player) {
        var result = PrintingBehaviour.create(getWorld(), tank, stack)
                .resultOrPartial(message -> {
                    if (player != null)
                        player.displayClientMessage(Component.translatable(message), true);
                });
        if (result.isPresent() && super.setFilter(stack)) {
            printing = result.get();
            return true;
        }
        return false;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        return setFilter(stack, null);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.put(TEMPLATE, getFilter().save(new CompoundTag()));
    }

    @Override
    public void writeSafe(CompoundTag nbt) {
        if (printing.isSafeNBT())
            nbt.put(TEMPLATE, getFilter().save(new CompoundTag()));
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        var filter = FilterItemStack.of(ItemStack.of(nbt.getCompound(TEMPLATE)));
        var printing = PrintingBehaviour.create(getWorld(), tank, filter.item()).result();
        if (printing.isPresent()) {
            this.filter = filter;
            this.printing = printing.get();
        } else {
            this.filter = FilterItemStack.empty();
            this.printing = RecipePrintingBehaviour.EMPTY;
        }
    }

    @Override
    public String getClipboardKey() {
        return "Printer";
    }

    @Override
    public boolean writeToClipboard(CompoundTag tag, Direction side) {
        ItemStack template = getFilter();
        tag.put(TEMPLATE, template.save(new CompoundTag()));
        return true;
    }

    @Override
    public boolean readFromClipboard(CompoundTag tag, Player player, Direction side, boolean simulate) {
        if (!tag.contains(TEMPLATE))
            return false;
        ItemStack template = ItemStack.of(tag.getCompound(TEMPLATE));
        return setFilter(template, player);
    }

    @Override
    @CodeReference(value = FilteringBehaviour.class, targets = "onShortInteract", source = "create", license = "mit")
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        Level level = getWorld();
        BlockPos pos = getPos();
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack toApply = itemInHand.copy();

        if (!canShortInteract(toApply))
            return;
        if (level.isClientSide())
            return;

        if (getFilter().getItem() instanceof FilterItem) {
            if (!player.isCreative() || ItemHelper.extract(new InvWrapper(player.getInventory()), stack -> ItemStack.isSameItemSameTags(stack, getFilter(side)), true).isEmpty())
                player.getInventory().placeItemBackInInventory(getFilter(side).copy());
        }

        if (toApply.getItem() instanceof FilterItem)
            toApply.setCount(1);

        if (!setFilter(toApply, player)) {
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }

        if (!player.isCreative()) {
            if (toApply.getItem() instanceof FilterItem) {
                if (itemInHand.getCount() == 1)
                    player.setItemInHand(hand, ItemStack.EMPTY);
                else
                    itemInHand.shrink(1);
            }
        }

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }
}
