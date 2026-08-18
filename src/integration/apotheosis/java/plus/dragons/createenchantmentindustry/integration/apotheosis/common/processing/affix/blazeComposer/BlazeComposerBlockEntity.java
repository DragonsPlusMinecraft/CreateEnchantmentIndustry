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

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.integration.apotheosis.client.registry.CEIAXPartialModels;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateDisplay;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateItem;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateOps;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXItems;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXStats;
import plus.dragons.createenchantmentindustry.integration.apotheosis.config.CEIAXConfig;
import plus.dragons.createenchantmentindustry.util.BlazeLightningHelper;
import plus.dragons.createenchantmentindustry.util.CEILang;

@FieldsNullabilityUnknownByDefault
public class BlazeComposerBlockEntity extends BlazeBlockEntity implements Clearable, IHaveGoggleInformation {
    private static final int PENALTY_STEPS_PER_LEVEL = 100;
    protected int processingTime = -1;
    protected BlazeComposerMode mode = BlazeComposerMode.EXTRACT;
    protected boolean superMode;
    protected boolean superUnlocked;
    protected boolean lightningBlocked;
    protected boolean pendingBlockedSuperOperation;
    protected float pendingBlockedSuperPenalty;
    protected final BlazeComposerInventory inventory;
    protected BlazeComposerModeBehaviour modeSelector;
    protected FluidTankBehaviour tanks;
    protected IFluidHandler fuelHandler;
    protected AdvancementBehaviour advancement;
    protected @Nullable ActiveComposing activeComposing;
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.empty();
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.empty();

    public BlazeComposerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new BlazeComposerInventory(this);
        this.itemCapability = LazyOptional.of(() -> inventory);
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if ((side == Direction.DOWN || side == null) && !isRemoved())
            return fuelHandler;
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        modeSelector = new BlazeComposerModeBehaviour(this, new ModeTransform());
        tanks = new FluidTankBehaviour(this, List.of(this::createNormalTank, this::createSuperTank), false);
        fuelHandler = new SuperFuelFluidHandler(this::getNormalTank, this::getSuperTank, this::canFillSuperTank);
        fluidCapability = LazyOptional.of(() -> fuelHandler);
        advancement = new AdvancementBehaviour(this);
        behaviours.add(modeSelector);
        behaviours.add(tanks);
        behaviours.add(advancement);
    }

    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIAXConfig.server().affixes().blazeComposerFluidCapacity.get(), fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.getFluid() == CEIAXFluids.APOTHEOTIC_ESSENCE.get());
    }

    protected ConfigurableFluidTank createSuperTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIAXConfig.server().affixes().blazeComposerSuperFluidCapacity.get(), fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.getFluid() == CEIAXFluids.APOTHEOTIC_ESSENCE.get());
    }

    @Override
    public boolean isActive() {
        return processingTime > 0;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public HeatLevel getHeatLevel() {
        if (getSuperEssence() > 0)
            return HeatLevel.SEETHING;
        return getNormalEssence() > 0 ? HeatLevel.KINDLED : HeatLevel.SMOULDERING;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected @Nullable PartialModel getHatModel(HeatLevel heatLevel) {
        return heatLevel.isAtLeast(HeatLevel.FADING)
                ? CEIAXPartialModels.BLAZE_COMPOSER_HAT
                : CEIAXPartialModels.BLAZE_COMPOSER_HAT_SMALL;
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("ProcessingTime", processingTime);
        compound.putInt("Mode", mode.ordinal());
        compound.putBoolean("SuperUnlocked", superUnlocked);
        compound.putBoolean("PendingBlockedSuperOperation", pendingBlockedSuperOperation);
        compound.putFloat("PendingBlockedSuperPenalty", pendingBlockedSuperPenalty);
        compound.put("Inventory", inventory.serializeNBT());
        if (activeComposing != null)
            compound.put("ActiveComposing", activeComposing.save());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        mode = BlazeComposerMode.BY_ID.apply(compound.getInt("Mode"));
        superUnlocked = compound.getBoolean("SuperUnlocked");
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        processingTime = compound.contains("ProcessingTime") ? compound.getInt("ProcessingTime") : -1;
        pendingBlockedSuperOperation = compound.getBoolean("PendingBlockedSuperOperation");
        pendingBlockedSuperPenalty = compound.getFloat("PendingBlockedSuperPenalty");
        activeComposing = compound.contains("ActiveComposing", Tag.TAG_COMPOUND)
                ? ActiveComposing.load(compound.getCompound("ActiveComposing"))
                : null;
        if (processingTime >= 0 && activeComposing == null) {
            processingTime = -1;
            clearPendingSuperOperation();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null)
            ItemHelper.dropContents(level, worldPosition, inventory);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;
        if (!Apotheosis.enableAdventure) {
            cancelProcessing();
            return;
        }
        if (level.isClientSide() && !isVirtual())
            return;
        boolean superMode = isSuper();
        if (this.superMode != superMode) {
            this.superMode = superMode;
            inventory.updateResult();
            notifyUpdate();
        }
        boolean lightningBlocked = isSuperLightningBlocked();
        if (this.lightningBlocked != lightningBlocked) {
            this.lightningBlocked = lightningBlocked;
            inventory.updateResult();
            notifyUpdate();
        }
        if (activeComposing == null) {
            startProcessing(superMode);
            return;
        }
        ActiveComposing active = activeComposing;
        if (!active.matches(inventory.getStackInSlot(0), inventory.getStackInSlot(1))) {
            cancelProcessing();
            inventory.updateResult();
            return;
        }
        if (!consumeEssence(active.cost(), active.superMode(), true))
            return;
        if (processingTime > 0) {
            processingTime--;
            notifyUpdate();
            return;
        }
        if (active.strikeLightning()
                && level instanceof ServerLevel serverLevel
                && BlazeLightningHelper.strikeLightning(serverLevel, worldPosition)) {
            advancement.trigger(CEIAdvancements.OSHA_VIOLATION.builtinTrigger());
            serverLevel.destroyBlock(worldPosition, false);
            serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
            this.setRemoved();
            return;
        }
        if (!consumeEssence(active.cost(), active.superMode(), false))
            return;
        inventory.applyResult(active.primaryOutput(), active.secondaryOutput());
        finishProcessing();
        advancement.awardStat(CEIAXStats.COMPOSE_AFFIX.get(), 1);
        notifyUpdate();
        level.playSound(null, worldPosition, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.8F, 0.9F + 0.2F * level.random.nextFloat());
        level.playSound(null, worldPosition, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.5F, 0.7F + 0.2F * level.random.nextFloat());
    }

    private boolean startProcessing(boolean superMode) {
        AffixTemplateOps.Result preview = inventory.getLastResult();
        if (!preview.valid()
                || inventory.hasRemainingOutput()
                || !consumeEssence(preview.cost(), superMode, true))
            return false;
        beginProcessing(superMode);
        AffixTemplateOps.Result result = inventory.getProcessingResult();
        if (!result.valid()) {
            clearPendingSuperOperation();
            inventory.updateResult();
            return false;
        }
        activeComposing = new ActiveComposing(
                inventory.getStackInSlot(0).copy(),
                inventory.getStackInSlot(1).copy(),
                result.primaryOutput().copy(),
                result.secondaryOutput().copy(),
                result.cost(),
                superMode,
                superMode && !pendingBlockedSuperOperation);
        processingTime = processingTime();
        notifyUpdate();
        return true;
    }

    private void finishProcessing() {
        processingTime = -1;
        activeComposing = null;
        clearPendingSuperOperation();
    }

    private void cancelProcessing() {
        if (processingTime != -1 || activeComposing != null || pendingBlockedSuperOperation) {
            finishProcessing();
            notifyUpdate();
        }
    }

    protected void beginProcessing(boolean superMode) {
        clearPendingSuperOperation();
        if (!superMode || !isSuperLightningBlocked())
            return;
        pendingBlockedSuperOperation = true;
        pendingBlockedSuperPenalty = randomBlockedSuperPenalty();
        inventory.updateResult();
    }

    public void clearPendingSuperOperation() {
        pendingBlockedSuperOperation = false;
        pendingBlockedSuperPenalty = 0;
    }

    public void onInputChanged() {
        cancelProcessing();
    }

    public float getBlockedSuperPenalty() {
        return pendingBlockedSuperOperation && isSuper() ? pendingBlockedSuperPenalty : 0;
    }

    public float getBlockedSuperPreviewMinPenalty() {
        return shouldPreviewBlockedSuperPenalty() ? minBlockedSuperPenalty() : 0;
    }

    public float getBlockedSuperPreviewMaxPenalty() {
        return shouldPreviewBlockedSuperPenalty() ? maxBlockedSuperPenalty() : 0;
    }

    public boolean shouldPreviewBlockedSuperPenalty() {
        return isSuper() && (pendingBlockedSuperOperation || isSuperLightningBlocked());
    }

    public boolean isSuperLightningBlocked() {
        if (level == null || !isSuper())
            return false;
        return BlazeLightningHelper.isStrikeBlocked(worldPosition, BlazeLightningHelper.getStrikePos(level, worldPosition));
    }

    protected float randomBlockedSuperPenalty() {
        if (level == null)
            return minBlockedSuperPenalty();
        int minStep = (int) Math.ceil(minBlockedSuperPenalty() * PENALTY_STEPS_PER_LEVEL);
        int maxStep = (int) Math.floor(maxBlockedSuperPenalty() * PENALTY_STEPS_PER_LEVEL);
        if (maxStep < minStep)
            return minBlockedSuperPenalty();
        return (minStep + level.random.nextInt(maxStep - minStep + 1)) / (float) PENALTY_STEPS_PER_LEVEL;
    }

    protected float minBlockedSuperPenalty() {
        var config = CEIAXConfig.server().affixes();
        return Math.max(0, Math.min(
                config.blazeComposerBlockedSuperMinLevelPenalty.getF(),
                config.blazeComposerBlockedSuperMaxLevelPenalty.getF()));
    }

    protected float maxBlockedSuperPenalty() {
        var config = CEIAXConfig.server().affixes();
        return Math.max(0, Math.max(
                config.blazeComposerBlockedSuperMinLevelPenalty.getF(),
                config.blazeComposerBlockedSuperMaxLevelPenalty.getF()));
    }

    public boolean consumeEssence(int amount, boolean superMode, boolean simulate) {
        var fluid = new FluidStack(CEIAXFluids.APOTHEOTIC_ESSENCE.get(), amount);
        var tank = superMode ? getSuperTank() : getNormalTank();
        var drained = tank.drain(fluid, FluidAction.SIMULATE);
        if (drained.getAmount() != amount)
            return false;
        if (!simulate)
            tank.drain(fluid, FluidAction.EXECUTE);
        return true;
    }

    public SmartFluidTank getNormalTank() {
        return tanks.getHandlers()[0];
    }

    public SmartFluidTank getSuperTank() {
        return tanks.getHandlers()[1];
    }

    public int getNormalEssence() {
        return getNormalTank().getFluidAmount();
    }

    public int getSuperEssence() {
        return getSuperTank().getFluidAmount();
    }

    public boolean isSuper() {
        return getSuperEssence() > 0;
    }

    public boolean isSuperUnlocked() {
        return superUnlocked;
    }

    public boolean canFillSuperTank() {
        return superUnlocked || getSuperEssence() > 0;
    }

    public BlazeComposerMode getMode() {
        return mode;
    }

    public void setMode(BlazeComposerMode mode) {
        if (this.mode == mode)
            return;
        this.mode = mode;
        finishProcessing();
        inventory.updateResult();
        notifyUpdate();
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (!Apotheosis.enableAdventure)
            return stack;
        var original = stack;
        if (inventory.hasRemainingOutput())
            return stack;
        stack = unlockSuper(stack, simulate);
        if (!ItemStack.isSameItemSameTags(original, stack) || original.getCount() != stack.getCount())
            return stack;
        if (isSuperActivator(stack))
            return stack;
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
        if (!Apotheosis.enableAdventure)
            return stack;
        if (stack.isEmpty())
            return stack;
        if (FluidUtil.getFluidHandler(stack).isPresent())
            return emptyFluidContainer(stack, simulate);
        var original = stack;
        if (inventory.hasRemainingOutput() || hasRecoverableAutomationInput())
            return stack;
        stack = unlockSuper(stack, simulate);
        if (insertedAny(original, stack))
            return stack;
        if (isSuperActivator(stack))
            return stack;
        int slot = getAutomationInsertionSlot(stack);
        if (slot < 0)
            return stack;
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

    private ItemStack emptyFluidContainer(ItemStack stack, boolean simulate) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(stack, getFluidHandler(null), Integer.MAX_VALUE, null, !simulate);
        return result.isSuccess() ? result.getResult() : stack;
    }

    private int getAutomationInsertionSlot(ItemStack stack) {
        return switch (mode) {
            case EXTRACT -> {
                if (isAffixedEquipment(stack))
                    yield inventory.getStackInSlot(0).isEmpty() ? 0 : -1;
                if (isBlankMatchingTemplate(stack))
                    yield inventory.getStackInSlot(1).isEmpty() ? 1 : -1;
                yield -1;
            }
            case APPLY -> {
                if (isAffixEquipment(stack))
                    yield inventory.getStackInSlot(0).isEmpty() ? 0 : -1;
                if (isFilledMatchingTemplate(stack))
                    yield inventory.getStackInSlot(1).isEmpty() ? 1 : -1;
                yield -1;
            }
            case MERGE -> {
                if (!isFilledMatchingTemplate(stack))
                    yield -1;
                if (inventory.getStackInSlot(0).isEmpty())
                    yield 0;
                if (inventory.getStackInSlot(1).isEmpty())
                    yield 1;
                yield -1;
            }
        };
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
        if (FluidUtil.getFluidHandler(stack).isPresent() || isSuperActivator(stack))
            return true;
        if (!isExpectedAutomationInput(slot, stack))
            return true;
        return !inventory.getStackInSlot(0).isEmpty()
                && !inventory.getStackInSlot(1).isEmpty()
                && inventory.getLastResult().status() == AffixTemplateOps.Status.INVALID;
    }

    private boolean isExpectedAutomationInput(int slot, ItemStack stack) {
        return switch (mode) {
            case EXTRACT -> slot == 0 ? isAffixedEquipment(stack) : isBlankMatchingTemplate(stack);
            case APPLY -> slot == 0 ? isAffixEquipment(stack) : isFilledMatchingTemplate(stack);
            case MERGE -> isFilledMatchingTemplate(stack);
        };
    }

    private boolean isBlankMatchingTemplate(ItemStack stack) {
        AffixTemplateItem template = AffixTemplateOps.getTemplateItem(stack);
        return template != null
                && template.tier().matchesSuperMode(isSuper())
                && AffixTemplateOps.isBlankTemplate(stack);
    }

    private boolean isFilledMatchingTemplate(ItemStack stack) {
        AffixTemplateItem template = AffixTemplateOps.getTemplateItem(stack);
        return template != null
                && template.tier().matchesSuperMode(isSuper())
                && AffixTemplateOps.isFilledTemplate(stack);
    }

    private static boolean isAffixedEquipment(ItemStack stack) {
        return isAffixEquipment(stack) && !AffixHelper.getAffixes(stack).isEmpty();
    }

    private static boolean isAffixEquipment(ItemStack stack) {
        return !stack.isEmpty()
                && AffixTemplateOps.getTemplateItem(stack) == null
                && !LootCategory.forItem(stack).isNone();
    }

    private static boolean insertedAny(ItemStack original, ItemStack remainder) {
        return original.getCount() != remainder.getCount()
                || !ItemStack.isSameItemSameTags(original, remainder);
    }

    public boolean isSuperActivator(ItemStack stack) {
        return stack.is(CEIAXItems.MOD_TAGS.blazeComposerSuperActivators);
    }

    public boolean canUnlockSuper(ItemStack stack) {
        return !stack.isEmpty()
                && isSuperActivator(stack)
                && !superUnlocked
                && getSuperEssence() == 0
                && getNormalEssence() >= getNormalTank().getCapacity();
    }

    public ItemStack unlockSuper(ItemStack stack, boolean simulate) {
        if (!canUnlockSuper(stack))
            return stack;
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        if (!simulate) {
            superUnlocked = true;
            finishProcessing();
            inventory.updateResult();
            notifyUpdate();
            if (level != null && !level.isClientSide()) {
                level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.35F, 1.6F + 0.2F * level.random.nextFloat());
                level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.45F, 0.75F + 0.2F * level.random.nextFloat());
            }
        }
        return remainder;
    }

    protected int processingTime() {
        int processingTime = CEIAXConfig.server().affixes().blazeComposerProcessingTime.get();
        return isVirtual() ? Math.max(1, processingTime / 4) : processingTime;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!Apotheosis.enableAdventure)
            return false;
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);
        addTankTooltip(tooltip, mb, "gui.goggles.blaze_composer.normal_essence", getNormalTank(), ChatFormatting.GOLD);
        addTankTooltip(tooltip, mb, "gui.goggles.blaze_composer.super_essence", getSuperTank(), ChatFormatting.BLUE);
        boolean superMode = isSuper();
        ChatFormatting essenceStyle = superMode ? ChatFormatting.BLUE : ChatFormatting.GOLD;
        CEILang.translate(
                "gui.goggles.blaze_composer.super_mode",
                CEILang.translate("gui.blaze_composer.super_mode." + (isSuper() ? "super" : "normal")).style(essenceStyle))
                .forGoggles(tooltip);
        CEILang.translate("gui.goggles.blaze_composer.mode", CEILang.translate("gui.blaze_composer.mode." + mode.getSerializedName()).style(ChatFormatting.AQUA))
                .forGoggles(tooltip);
        addSuperLightningTooltip(tooltip);
        if (inventory.hasRemainingOutput()) {
            CEILang.translate("gui.goggles.blaze_composer.output_blocked").style(ChatFormatting.YELLOW).forGoggles(tooltip);
            return true;
        }
        var result = inventory.getLastResult();
        if (result.status() == AffixTemplateOps.Status.EMPTY_INPUT) {
            addModeHelp(tooltip);
        } else if (result.status() == AffixTemplateOps.Status.INCOMPLETE_INPUT) {
            addModeHelp(tooltip);
            CEILang.builder().add(result.failure().copy()).style(ChatFormatting.YELLOW).forGoggles(tooltip, 1);
        } else if (result.status() == AffixTemplateOps.Status.INVALID) {
            CEILang.builder().add(result.failure().copy()).style(ChatFormatting.RED).forGoggles(tooltip);
            for (Component description : result.warningDescriptions()) {
                CEILang.builder().add(description.copy()).style(ChatFormatting.YELLOW).forGoggles(tooltip, 1);
            }
        } else if (result.valid()) {
            int cost = result.cost();
            CEILang.translate("gui.goggles.blaze_composer.cost", CEILang.number(cost).add(mb).style(essenceStyle))
                    .forGoggles(tooltip);
            CEILang.translate("gui.goggles.blaze_composer.result").forGoggles(tooltip);
            for (Component description : result.outputDescriptions()) {
                CEILang.builder().add(description.copy()).forGoggles(tooltip, 1);
            }
            if (!result.warningDescriptions().isEmpty()) {
                CEILang.translate("gui.goggles.blaze_composer.lost_affixes").style(ChatFormatting.YELLOW).forGoggles(tooltip);
                for (Component description : result.warningDescriptions()) {
                    CEILang.builder().add(description.copy()).style(ChatFormatting.YELLOW).forGoggles(tooltip, 1);
                }
            }
            int essence = superMode ? getSuperEssence() : getNormalEssence();
            if (essence < cost) {
                CEILang.translate(
                        superMode ? "gui.goggles.blaze_composer.insufficient_super_essence" : "gui.goggles.blaze_composer.insufficient_essence",
                        CEILang.number(essence).add(mb).style(essenceStyle),
                        CEILang.number(cost).add(mb).style(essenceStyle))
                        .style(ChatFormatting.RED)
                        .forGoggles(tooltip);
            }
        }
        return true;
    }

    private void addTankTooltip(List<Component> tooltip, LangBuilder mb, String labelKey, SmartFluidTank tank, ChatFormatting amountStyle) {
        CEILang.translate(labelKey).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        CreateLang.builder()
                .add(CreateLang.number(tank.getFluidAmount()).add(mb).style(amountStyle))
                .text(ChatFormatting.GRAY, " / ")
                .add(CreateLang.number(tank.getCapacity()).add(mb).style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 2);
    }

    private void addModeHelp(List<Component> tooltip) {
        CEILang.translate("gui.goggles.blaze_composer.mode_help." + mode.getSerializedName())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.translate("gui.goggles.blaze_composer.requires").forGoggles(tooltip);
        CEILang.translate(
                "gui.goggles.blaze_composer.requires.first",
                CEILang.translate("gui.goggles.blaze_composer.requires." + mode.getSerializedName() + ".first").component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.translate(
                "gui.goggles.blaze_composer.requires.second",
                CEILang.translate("gui.goggles.blaze_composer.requires." + mode.getSerializedName() + ".second").component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private void addSuperLightningTooltip(List<Component> tooltip) {
        if (shouldPreviewBlockedSuperPenalty()) {
            CEILang.translate(
                    "gui.goggles.blaze_composer.blocked_super_penalty.range",
                    AffixTemplateDisplay.formatLevel(minBlockedSuperPenalty()),
                    AffixTemplateDisplay.formatLevel(maxBlockedSuperPenalty()))
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !isRemoved())
            return itemCapability.cast();
        if (capability == ForgeCapabilities.FLUID_HANDLER
                && !isRemoved()
                && (side == null || side == Direction.DOWN))
            return fluidCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
        fluidCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> inventory);
        fluidCapability = LazyOptional.of(() -> fuelHandler);
    }

    @Override
    public void clearContent() {
        finishProcessing();
        inventory.clear();
    }

    protected record ActiveComposing(
            ItemStack firstInput,
            ItemStack secondInput,
            ItemStack primaryOutput,
            ItemStack secondaryOutput,
            int cost,
            boolean superMode,
            boolean strikeLightning) {
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("FirstInput", firstInput.save(new CompoundTag()));
            tag.put("SecondInput", secondInput.save(new CompoundTag()));
            tag.put("PrimaryOutput", primaryOutput.save(new CompoundTag()));
            tag.put("SecondaryOutput", secondaryOutput.save(new CompoundTag()));
            tag.putInt("Cost", cost);
            tag.putBoolean("SuperMode", superMode);
            tag.putBoolean("StrikeLightning", strikeLightning);
            return tag;
        }

        static @Nullable ActiveComposing load(CompoundTag tag) {
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
            return new ActiveComposing(
                    firstInput,
                    secondInput,
                    primaryOutput,
                    secondaryOutput,
                    cost,
                    tag.getBoolean("SuperMode"),
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
