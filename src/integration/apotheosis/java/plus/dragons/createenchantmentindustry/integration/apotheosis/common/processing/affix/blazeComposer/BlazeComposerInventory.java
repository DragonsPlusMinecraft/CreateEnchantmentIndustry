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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateOps;

public class BlazeComposerInventory extends ItemStackHandler {
    private final BlazeComposerBlockEntity composer;
    private AffixTemplateOps.Result result = AffixTemplateOps.Result.emptyInput();

    public BlazeComposerInventory(BlazeComposerBlockEntity composer) {
        super(4);
        this.composer = composer;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public int getSlots() {
        return 4;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot > 1)
            return stack;
        if (hasRemainingOutput())
            return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    protected void onLoad() {
        var level = composer.getLevel();
        if (level != null && !level.isClientSide)
            updateResult();
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (slot == 0 || slot == 1) {
            composer.onInputChanged();
            updateResult();
        }
        composer.notifyUpdate();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        updateResult();
    }

    @Override
    public CompoundTag serializeNBT() {
        return super.serializeNBT();
    }

    public int getEssenceCost() {
        return result.cost();
    }

    public AffixTemplateOps.Result getLastResult() {
        return result;
    }

    public boolean hasRemainingOutput() {
        return !stacks.get(2).isEmpty() || !stacks.get(3).isEmpty();
    }

    public void clearInput() {
        stacks.set(0, ItemStack.EMPTY);
        stacks.set(1, ItemStack.EMPTY);
        result = AffixTemplateOps.Result.emptyInput();
    }

    public void clear() {
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        result = AffixTemplateOps.Result.emptyInput();
    }

    public AffixTemplateOps.Result getProcessingResult() {
        return AffixTemplateOps.compose(
                composer.getMode(),
                composer.isSuper(),
                composer.getBlockedSuperPenalty(),
                stacks.get(0),
                stacks.get(1));
    }

    public void applyResult(ItemStack primaryOutput, ItemStack secondaryOutput) {
        if (primaryOutput.isEmpty() && secondaryOutput.isEmpty())
            return;
        stacks.set(2, primaryOutput.copy());
        stacks.set(3, secondaryOutput.copy());
        clearInput();
        updateResult();
    }

    public void updateResult() {
        result = AffixTemplateOps.compose(
                composer.getMode(),
                composer.isSuper(),
                0,
                composer.getBlockedSuperPreviewMinPenalty(),
                composer.getBlockedSuperPreviewMaxPenalty(),
                stacks.get(0),
                stacks.get(1));
    }
}
