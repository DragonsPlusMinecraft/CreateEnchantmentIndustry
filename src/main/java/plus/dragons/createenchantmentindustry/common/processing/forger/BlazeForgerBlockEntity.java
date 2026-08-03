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

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import java.util.List;
import java.util.function.Consumer;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.BlazeLightningHelper;
import plus.dragons.createenchantmentindustry.util.CEILang;

@FieldsNullabilityUnknownByDefault
public class BlazeForgerBlockEntity extends BlazeExperienceBlockEntity implements Clearable {
    public static final int FORGING_TIME = 200;
    protected BlazeForgerMode mode = BlazeForgerMode.MERGE;
    protected boolean special;
    protected boolean cursed;
    protected int processingTime = -1;
    protected final BlazeForgerInventory inventory;
    protected BlazeForgerModeBehaviour modeSelector;
    protected AdvancementBehaviour advancement;
    protected @Nullable ActiveForging activeForging;

    public BlazeForgerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new BlazeForgerInventory(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.modeSelector = new BlazeForgerModeBehaviour(this, new ModeTransform());
        this.advancement = new AdvancementBehaviour(this);
        behaviours.add(this.modeSelector);
        behaviours.add(this.advancement);
    }

    @Override
    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeForgerFluidCapacity.get(), fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.getFluid() == CEIFluids.EXPERIENCE.get());
    }

    @Override
    protected ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeForgerFluidCapacity.get(), fluidUpdateCallback)
                .forbidInsertion();
    }

    @Override
    public boolean isActive() {
        return processingTime > 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected @Nullable PartialModel getHatModel(HeatLevel heatLevel) {
        return heatLevel.isAtLeast(HeatLevel.FADING)
                ? CEIPartialModels.BLAZE_FORGER_HAT
                : CEIPartialModels.BLAZE_FORGER_HAT_SMALL;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("ProcessingTime", processingTime);
        compound.putInt("ForgingMode", mode.ordinal());
        compound.put("Inventory", inventory.serializeNBT());
        if (activeForging != null)
            compound.put("ActiveForging", activeForging.save());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        processingTime = compound.getInt("ProcessingTime");
        if (compound.contains("ForgingMode", Tag.TAG_INT)) {
            mode = BlazeForgerMode.BY_ID.apply(compound.getInt("ForgingMode"));
        } else {
            // TODO Remove this legacy fallback after pre-mode-panel Blaze Forger saves no longer need conversion.
            CompoundTag inventoryTag = compound.getCompound("Inventory");
            if (inventoryTag.contains("Mode", Tag.TAG_INT))
                mode = BlazeForgerMode.fromLegacyOperation(inventoryTag.getInt("Mode"));
        }
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        activeForging = compound.contains("ActiveForging", Tag.TAG_COMPOUND)
                ? ActiveForging.load(compound.getCompound("ActiveForging"))
                : null;
        if (processingTime >= 0 && activeForging == null)
            processingTime = -1;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) {
            ItemHelper.dropContents(level, worldPosition, inventory);
        }
    }

    @Override
    public void tick() {
        super.tick();
        boolean update = false;
        boolean special = getHeatLevelFromBlock() == HeatLevel.SEETHING;
        if (this.special != special) {
            this.special = special;
            update = true;
        }
        var strikePos = getStrikePos();
        boolean cursed = special && BlazeLightningHelper.isStrikeBlocked(worldPosition, strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
            update = true;
        }
        if (level.isClientSide() && isVirtual()) {
            if (update) {
                inventory.updateResult();
                notifyUpdate();
            }
            tickVirtual();
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (update) {
            inventory.updateResult();
            notifyUpdate();
        }
        if (activeForging == null) {
            startProcessing(FORGING_TIME, true);
            return;
        }
        ActiveForging active = activeForging;
        if (!active.matches(inventory.getStackInSlot(0), inventory.getStackInSlot(1))) {
            cancelProcessing();
            return;
        }
        if (!consumeExperience(active.cost(), active.special(), true))
            return;
        if (processingTime > 0) {
            processingTime--;
            notifyUpdate();
            return;
        }
        if (active.strikeLightning() && strikeLightning(serverLevel, strikePos)) {
            advancement.trigger(CEIAdvancements.OSHA_VIOLATION.builtinTrigger());
            serverLevel.destroyBlock(worldPosition, false);
            serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
            this.setRemoved();
            return;
        }
        if (!consumeExperience(active.cost(), active.special(), false))
            return;
        inventory.applyResult(
                active.primaryOutput(),
                active.secondaryOutput(),
                active.operation(),
                active.conflicting(),
                active.overCap(),
                active.special());
        finishProcessing();
        notifyUpdate();
        level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    private void tickVirtual() {
        if (activeForging == null) {
            startProcessing(FORGING_TIME / 4, false);
            return;
        }
        ActiveForging active = activeForging;
        if (!active.matches(inventory.getStackInSlot(0), inventory.getStackInSlot(1))) {
            cancelProcessing();
            return;
        }
        if (processingTime > 0) {
            processingTime--;
            return;
        }
        consumeExperience(active.cost(), active.special(), false);
        inventory.applyResult(
                active.primaryOutput(),
                active.secondaryOutput(),
                active.operation(),
                active.conflicting(),
                active.overCap(),
                active.special());
        finishProcessing();
    }

    private boolean startProcessing(int duration, boolean requireExperience) {
        BlazeForgerInventory.Result result = inventory.getLastResult();
        int cost = result.experienceCost();
        if (!result.valid()
                || inventory.hasRemainingOutput()
                || cost <= 0
                || requireExperience && !consumeExperience(cost, special, true))
            return false;
        activeForging = new ActiveForging(
                inventory.getStackInSlot(0).copy(),
                inventory.getStackInSlot(1).copy(),
                result.primaryOutput().copy(),
                result.secondaryOutput().copy(),
                cost,
                result.operation(),
                result.conflicting(),
                result.overCap(),
                special,
                special && !cursed);
        processingTime = duration;
        notifyUpdate();
        return true;
    }

    private void finishProcessing() {
        processingTime = -1;
        activeForging = null;
    }

    private void cancelProcessing() {
        if (processingTime != -1 || activeForging != null) {
            finishProcessing();
            notifyUpdate();
        }
    }

    public BlazeForgerMode getMode() {
        return mode;
    }

    public void setMode(BlazeForgerMode mode) {
        if (this.mode == mode)
            return;
        this.mode = mode;
        finishProcessing();
        inventory.updateResult();
        notifyUpdate();
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        var original = stack;
        if (inventory.hasRemainingOutput()) return stack;
        if (!stack.isEmpty())
            stack = inventory.insertItem(0, stack, simulate);
        if (!stack.isEmpty())
            stack = inventory.insertItem(1, stack, simulate);
        if (!simulate && (original.getCount() != stack.getCount() || !ItemStack.isSameItemSameTags(original, stack))) {
            inventory.updateResult();
            notifyUpdate();
        }
        return stack;
    }

    public ItemStack extractItem(boolean simulate) {
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack extracted = inventory.extractItem(i, 1, simulate);
            if (!extracted.isEmpty()) {
                if (!simulate && i < 2) {
                    inventory.updateResult();
                    notifyUpdate();
                }
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack insertAutomationItem(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || inventory.hasRemainingOutput() || hasRecoverableAutomationInput())
            return stack;
        int slot = getAutomationInsertionSlot(stack);
        if (slot < 0)
            return stack;
        ItemStack original = stack.copy();
        ItemStack remainder = inventory.insertItem(slot, stack, simulate);
        if (!simulate && insertedAny(original, remainder)) {
            inventory.updateResult();
            notifyUpdate();
        }
        return remainder;
    }

    public ItemStack extractAutomationItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || amount <= 0)
            return ItemStack.EMPTY;
        if (slot < 2)
            return inventory.extractItem(slot + 2, amount, simulate);
        int inputSlot = slot - 2;
        if (inputSlot > 1 || !isRecoverableAutomationInput(inputSlot))
            return ItemStack.EMPTY;
        ItemStack extracted = inventory.extractItem(inputSlot, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            inventory.updateResult();
            notifyUpdate();
        }
        return extracted;
    }

    public int getAutomationSlotCount() {
        return 4;
    }

    private int getAutomationInsertionSlot(ItemStack stack) {
        return switch (mode) {
            case MERGE -> firstEmptyInputSlot();
            case APPLY -> {
                if (isForgingAddition(stack))
                    yield inventory.getStackInSlot(1).isEmpty() ? 1 : -1;
                if (isForgingTarget(stack))
                    yield inventory.getStackInSlot(0).isEmpty() ? 0 : -1;
                yield -1;
            }
            case EXTRACT -> {
                if (isBlankMatchingTemplate(stack))
                    yield inventory.getStackInSlot(1).isEmpty() ? 1 : -1;
                if (isExtractingSource(stack))
                    yield inventory.getStackInSlot(0).isEmpty() ? 0 : -1;
                yield -1;
            }
        };
    }

    private int firstEmptyInputSlot() {
        if (inventory.getStackInSlot(0).isEmpty())
            return 0;
        if (inventory.getStackInSlot(1).isEmpty())
            return 1;
        return -1;
    }

    private boolean hasRecoverableAutomationInput() {
        return isRecoverableAutomationInput(0) || isRecoverableAutomationInput(1);
    }

    private boolean isRecoverableAutomationInput(int slot) {
        if (processingTime >= 0 || inventory.hasRemainingOutput())
            return false;
        ItemStack stack = inventory.getStackInSlot(slot);
        if (stack.isEmpty())
            return false;
        if (!isExpectedAutomationInput(slot, stack))
            return true;
        return !inventory.getStackInSlot(0).isEmpty()
                && !inventory.getStackInSlot(1).isEmpty()
                && inventory.getLastResult().status() == BlazeForgerInventory.Status.INVALID;
    }

    private boolean isExpectedAutomationInput(int slot, ItemStack stack) {
        return switch (mode) {
            case MERGE -> isMergeInput(stack);
            case APPLY -> slot == 0 ? isForgingTarget(stack) : isForgingAddition(stack);
            case EXTRACT -> slot == 0 ? isExtractingSource(stack) : isBlankMatchingTemplate(stack);
        };
    }

    private boolean isMergeInput(ItemStack stack) {
        return !stack.isEmpty() && !isBlankTemplate(stack);
    }

    private boolean isForgingAddition(ItemStack stack) {
        if (isFilledMatchingTemplate(stack))
            return true;
        return stack.is(Items.ENCHANTED_BOOK) && hasEnchantments(stack);
    }

    private boolean isForgingTarget(ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof EnchantingTemplateItem)
                && !stack.is(Items.ENCHANTED_BOOK);
    }

    private boolean isExtractingSource(ItemStack stack) {
        return !stack.isEmpty() && !isBlankTemplate(stack) && hasEnchantments(stack);
    }

    private boolean isBlankMatchingTemplate(ItemStack stack) {
        return isTemplateMatchingMode(stack) && !hasEnchantments(stack);
    }

    private boolean isFilledMatchingTemplate(ItemStack stack) {
        return isTemplateMatchingMode(stack) && hasEnchantments(stack);
    }

    private boolean isBlankTemplate(ItemStack stack) {
        return stack.getItem() instanceof EnchantingTemplateItem && !hasEnchantments(stack);
    }

    private boolean isTemplateMatchingMode(ItemStack stack) {
        return stack.getItem() instanceof EnchantingTemplateItem template && template.isSpecial() == special;
    }

    private static boolean hasEnchantments(ItemStack stack) {
        return !CEIItemData.getEnchantmentsForCrafting(stack).isEmpty();
    }

    private static boolean insertedAny(ItemStack original, ItemStack remainder) {
        return original.getCount() != remainder.getCount()
                || !ItemStack.isSameItemSameTags(original, remainder);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        var style = special
                ? (cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CEILang.translate(
                "gui.goggles.forging.blaze_mode",
                CEILang.translate("gui.blaze_forger.blaze_mode." + (special ? "super" : "normal")).style(style))
                .forGoggles(tooltip);
        CEILang.translate("gui.goggles.forging.mode", CEILang.translate("gui.blaze_forger.mode." + mode.getSerializedName()).style(ChatFormatting.AQUA))
                .forGoggles(tooltip);
        addSuperLightningTooltip(tooltip);
        if (inventory.hasRemainingOutput()) {
            CEILang.translate("gui.goggles.forging.output_blocked").style(ChatFormatting.YELLOW).forGoggles(tooltip);
            return true;
        }
        var result = inventory.getLastResult();
        if (result.status() == BlazeForgerInventory.Status.EMPTY_INPUT) {
            addModeHelp(tooltip);
        } else if (result.status() == BlazeForgerInventory.Status.INCOMPLETE_INPUT) {
            addModeHelp(tooltip);
            CEILang.builder().add(result.failure().copy()).style(ChatFormatting.YELLOW).forGoggles(tooltip, 1);
        } else if (result.status() == BlazeForgerInventory.Status.INVALID) {
            CEILang.builder().add(result.failure().copy()).style(ChatFormatting.RED).forGoggles(tooltip);
            if (!result.rejectedEnchantments().isEmpty()) {
                CEILang.translate("gui.goggles.forging.rejected_enchantments").style(ChatFormatting.YELLOW).forGoggles(tooltip);
                for (Component description : result.rejectedEnchantments()) {
                    CEILang.builder().add(description.copy()).style(ChatFormatting.YELLOW).forGoggles(tooltip, 1);
                }
            }
        } else if (result.valid()) {
            added = true;
            int cost = result.experienceCost();
            CEILang.translate("gui.goggles.forging.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            CEILang.translate("gui.goggles.forging.result").forGoggles(tooltip);
            addOutputStack(tooltip, result.primaryOutput());
            addOutputStack(tooltip, result.secondaryOutput());
            if (result.overCap()) {
                CEILang.translate("gui.goggles.forging.over_cap").style(ChatFormatting.BLUE).forGoggles(tooltip, 1);
            }
            if (result.conflicting()) {
                CEILang.translate("gui.goggles.forging.conflicting").style(ChatFormatting.BLUE).forGoggles(tooltip, 1);
            }
            if (result.repairCostPenalty()) {
                CEILang.translate(
                        "gui.goggles.forging.repair_cost_penalty",
                        CEILang.number(result.repairCostBefore()).style(ChatFormatting.GRAY),
                        CEILang.number(result.repairCostAfter()).style(ChatFormatting.RED))
                        .style(ChatFormatting.RED)
                        .forGoggles(tooltip, 1);
            }
            if (!result.lostEnchantments().isEmpty()) {
                CEILang.translate("gui.goggles.forging.lost_enchantments").style(ChatFormatting.YELLOW).forGoggles(tooltip);
                for (Component description : result.lostEnchantments()) {
                    CEILang.builder().add(description.copy()).style(ChatFormatting.YELLOW).forGoggles(tooltip, 1);
                }
            }
            int experience = special ? getSpecialExperience() : getTotalExperience();
            if (experience < cost) {
                CEILang.translate(
                        special ? "gui.goggles.forging.insufficient_super_experience" : "gui.goggles.forging.insufficient_experience",
                        CEILang.number(experience).add(mb).style(style),
                        CEILang.number(cost).add(mb).style(style))
                        .style(ChatFormatting.RED)
                        .forGoggles(tooltip);
            }
        }
        return added;
    }

    private void addModeHelp(List<Component> tooltip) {
        CEILang.translate("gui.goggles.forging.mode_help." + mode.getSerializedName())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.translate("gui.goggles.forging.requires").forGoggles(tooltip);
        CEILang.translate(
                "gui.goggles.forging.requires.first",
                CEILang.translate("gui.goggles.forging.requires." + mode.getSerializedName() + ".first").component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.translate(
                "gui.goggles.forging.requires.second",
                CEILang.translate("gui.goggles.forging.requires." + mode.getSerializedName() + ".second").component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private void addSuperLightningTooltip(List<Component> tooltip) {
        if (special && cursed) {
            CEILang.translate("gui.goggles.forging.blocked_super_penalty")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
        }
    }

    private void addOutputStack(List<Component> tooltip, ItemStack stack) {
        if (stack.isEmpty())
            return;
        CEILang.item(stack).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        CEIItemData.getEnchantmentsForCrafting(stack).forEach((enchantment, enchantmentLevel) -> CEILang.builder().add(enchantment.getFullname(enchantmentLevel)).forGoggles(tooltip, 2));
    }

    @Override
    public void clearContent() {
        inventory.clear();
        finishProcessing();
    }

    protected record ActiveForging(
            ItemStack firstInput,
            ItemStack secondInput,
            ItemStack primaryOutput,
            ItemStack secondaryOutput,
            int cost,
            BlazeForgerMode operation,
            boolean conflicting,
            boolean overCap,
            boolean special,
            boolean strikeLightning) {
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("FirstInput", firstInput.save(new CompoundTag()));
            tag.put("SecondInput", secondInput.save(new CompoundTag()));
            tag.put("PrimaryOutput", primaryOutput.save(new CompoundTag()));
            tag.put("SecondaryOutput", secondaryOutput.save(new CompoundTag()));
            tag.putInt("Cost", cost);
            tag.putInt("Operation", operation.ordinal());
            tag.putBoolean("Conflicting", conflicting);
            tag.putBoolean("OverCap", overCap);
            tag.putBoolean("Special", special);
            tag.putBoolean("StrikeLightning", strikeLightning);
            return tag;
        }

        static @Nullable ActiveForging load(CompoundTag tag) {
            ItemStack firstInput = ItemStack.of(tag.getCompound("FirstInput"));
            ItemStack secondInput = ItemStack.of(tag.getCompound("SecondInput"));
            ItemStack primaryOutput = ItemStack.of(tag.getCompound("PrimaryOutput"));
            ItemStack secondaryOutput = ItemStack.of(tag.getCompound("SecondaryOutput"));
            int cost = tag.getInt("Cost");
            if (firstInput.isEmpty()
                    || secondInput.isEmpty()
                    || primaryOutput.isEmpty() && secondaryOutput.isEmpty()
                    || cost <= 0)
                return null;
            return new ActiveForging(
                    firstInput,
                    secondInput,
                    primaryOutput,
                    secondaryOutput,
                    cost,
                    BlazeForgerMode.BY_ID.apply(tag.getInt("Operation")),
                    tag.getBoolean("Conflicting"),
                    tag.getBoolean("OverCap"),
                    tag.getBoolean("Special"),
                    tag.getBoolean("StrikeLightning"));
        }

        boolean matches(ItemStack first, ItemStack second) {
            return same(firstInput, first) && same(secondInput, second);
        }

        private static boolean same(ItemStack expected, ItemStack actual) {
            return expected.getCount() == actual.getCount() && ItemStack.isSameItemSameTags(expected, actual);
        }
    }

    private static class ModeTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 13.5);
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack poseStack) {
            float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            TransformStack.of(poseStack).rotateYDegrees(yRot);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
