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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance.LowerBeltProcessingBehaviour;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.AffixOperationCosts;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.template.AffixTemplateDisplay;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class AffixAugmentorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    public static final int UNIT_PROCESSING_TIME = 200;
    public static final int COMPLETION_TICKS = 25;
    private static final int HELD_INPUT_TIMEOUT = 3;
    public int processingTicks = -1;
    public boolean powered;
    public float chargingPercentage;
    @Nullable
    private ActiveAugmenting activeAugmenting;
    @Nullable
    private AugmentingPreview heldPreview;
    private int heldPreviewTicks;
    private int heldInputTicks;

    public AffixAugmentorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        BeltProcessingBehaviour processing = new LowerBeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemEnters)
                .whileItemHeld(this::onItemHeld);
        behaviours.add(processing);
    }

    @Override
    public void tick() {
        super.tick();
        if (!Apotheosis.enableAdventure) {
            if (!level.isClientSide) {
                powered = false;
                cancelProcessing();
                clearHeldPreview();
            }
            return;
        }
        if (level.isClientSide) {
            if (powered && chargingPercentage < 1) {
                chargingPercentage = Math.min(chargingPercentage + 0.025f, 1);
            } else if (!powered && chargingPercentage > 0) {
                chargingPercentage = Math.max(0, chargingPercentage - 0.025f);
            }
        }
        if (!level.isClientSide && !isVirtual()) {
            var fluidTank = getExternalFluidTank();
            if (fluidTank.isEmpty()) {
                if (powered) {
                    powered = false;
                    notifyUpdate();
                }
            } else {
                var tank = fluidTank.get().getTankInventory();
                if (tank.getFluid().getFluid() == CEIAXFluids.APOTHEOTIC_ESSENCE.get()) {
                    if (!powered) {
                        powered = true;
                        notifyUpdate();
                    }
                } else {
                    if (powered) {
                        powered = false;
                        notifyUpdate();
                    }
                }
            }
            tickHeldInputTimeouts();
        }
        if (processingTicks >= 0) {
            if (powered) {
                if (!level.isClientSide && !hasValidActiveInput()) {
                    cancelProcessing();
                    return;
                }
                processingTicks--;
                if (level.isClientSide && processingTicks > 25) {
                    spawnParticles();
                }
            } else if (processingTicks != -1) {
                cancelProcessing();
            }
        }
    }

    private Optional<FluidTankBlockEntity> getExternalFluidTank() {
        assert level != null;
        var be = level.getBlockEntity(worldPosition.below(2));
        if (be instanceof FluidTankBlockEntity tank) return Optional.of(tank.getControllerBE());
        else return Optional.empty();
    }

    public BeltProcessingBehaviour.ProcessingResult onItemEnters(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (!Apotheosis.enableAdventure)
            return PASS;
        Level level = this.level;
        assert level != null;

        if (handler.blockEntity.isVirtual())
            return PASS;

        if (!AffixAugmenting.canAugment(transported.stack))
            return PASS;

        return HOLD;
    }

    public BeltProcessingBehaviour.ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (!Apotheosis.enableAdventure)
            return PASS;
        Level level = this.level;
        assert level != null;
        var context = getAugmentingContext(transported.stack);
        rememberHeldPreview(context);
        if (context.status().passesInput()) {
            cancelProcessing();
            return PASS;
        }
        refreshHeldInput();

        if (processingTicks > COMPLETION_TICKS) {
            if (!validateTransportedInput(transported.stack))
                return PASS;
            return HOLD;
        }

        if (processingTicks == -1) {
            return startProcessingIfReady(context);
        }

        if (activeAugmenting == null) {
            var resultData = context.analysis().result();
            if (resultData.isEmpty()) {
                cancelProcessing();
                return PASS;
            }
            activeAugmenting = ActiveAugmenting.from(resultData.get());
        }
        var active = activeAugmenting;
        var affix = active.resolveAffix();
        if (affix.isEmpty() || !active.matchesInput(transported.stack)) {
            cancelProcessing();
            return AffixAugmenting.canAugment(transported.stack) ? startProcessingIfReady(context) : PASS;
        }
        if (context.status() != AugmentingStatus.READY || !canPay(context.tank(), active.cost())) {
            cancelProcessing();
            return HOLD;
        }

        TransportedItemStack result = transported.copy();
        result.clearFanProcessingData();
        TransportedItemStack remains = null;
        if (result.stack.getCount() > 1) {
            remains = transported.copy();
            remains.stack.shrink(1);
            result.stack.setCount(1);
        }
        result.stack = AffixAugmenting.apply(result.stack, affix.get(), active.toLevel());
        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(List.of(result), remains));
        level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.8f, .9f + 0.2f * level.random.nextFloat());
        level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.24f, .72f + 0.2f * level.random.nextFloat());
        level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.32f, .35f + 0.7f * level.random.nextFloat());
        context.tank().get().getTankInventory().drain(active.cost(), IFluidHandler.FluidAction.EXECUTE);
        cancelProcessing();
        return HOLD;
    }

    private BeltProcessingBehaviour.ProcessingResult startProcessingIfReady(AugmentingContext context) {
        if (context.status() != AugmentingStatus.READY)
            return HOLD;
        var result = context.analysis().result();
        if (result.isEmpty())
            return HOLD;
        activeAugmenting = ActiveAugmenting.from(result.get());
        refreshHeldInput();
        processingTicks = UNIT_PROCESSING_TIME;
        notifyUpdate();
        return HOLD;
    }

    private void rememberHeldPreview(AugmentingContext context) {
        var result = context.analysis().result();
        if (result.isEmpty()) {
            clearHeldPreview();
            return;
        }
        AugmentingPreview preview = AugmentingPreview.from(context.status(), result.get());
        boolean changed = !preview.equals(heldPreview);
        heldPreview = preview;
        heldPreviewTicks = HELD_INPUT_TIMEOUT;
        refreshHeldInput();
        if (changed)
            notifyUpdate();
    }

    private void refreshHeldInput() {
        heldInputTicks = HELD_INPUT_TIMEOUT;
    }

    private void tickHeldInputTimeouts() {
        boolean update = false;
        if (heldPreviewTicks > 0 && --heldPreviewTicks == 0 && heldPreview != null) {
            heldPreview = null;
            update = true;
        }
        if (heldInputTicks > 0)
            heldInputTicks--;
        if (update)
            notifyUpdate();
    }

    private void clearHeldPreview() {
        if (heldPreview != null || heldPreviewTicks != 0) {
            heldPreview = null;
            heldPreviewTicks = 0;
            notifyUpdate();
        }
    }

    private boolean validateTransportedInput(ItemStack stack) {
        if (processingTicks < 0)
            return true;
        if (activeAugmenting == null)
            return AffixAugmenting.canAugment(stack);
        var affix = activeAugmenting.resolveAffix();
        if (affix.isEmpty() || !activeAugmenting.matchesInput(stack)) {
            cancelProcessing();
            return false;
        }
        return true;
    }

    private boolean hasValidActiveInput() {
        if (processingTicks < 0)
            return true;
        var depot = getDepotBlockEntity();
        if (depot.isEmpty())
            return heldInputTicks > 0;
        if (activeAugmenting == null)
            return getDepotInputStack(depot.get()).map(AffixAugmenting::canAugment).orElse(false);
        var affix = activeAugmenting.resolveAffix();
        return affix.isPresent() && getDepotInputStack(depot.get())
                .map(activeAugmenting::matchesInput)
                .orElse(false);
    }

    private void cancelProcessing() {
        if (processingTicks != -1 || activeAugmenting != null) {
            processingTicks = -1;
            activeAugmenting = null;
            heldInputTicks = 0;
            notifyUpdate();
        }
    }

    private AugmentingContext getAugmentingContext(ItemStack stack) {
        var fluidTank = getExternalFluidTank();
        var analysis = AffixAugmenting.analyze(stack);
        if (analysis.status() == AffixAugmenting.Status.EMPTY_INPUT)
            return AugmentingContext.withAnalysis(AugmentingStatus.EMPTY_INPUT, fluidTank, analysis);
        if (analysis.status() == AffixAugmenting.Status.NO_AFFIXES)
            return AugmentingContext.withAnalysis(AugmentingStatus.NO_AFFIXES, fluidTank, analysis);
        if (analysis.status() == AffixAugmenting.Status.NO_UPGRADEABLE_AFFIXES)
            return AugmentingContext.withAnalysis(AugmentingStatus.NO_UPGRADEABLE_AFFIXES, fluidTank, analysis);
        if (fluidTank.isEmpty())
            return AugmentingContext.withAnalysis(AugmentingStatus.MISSING_TANK, fluidTank, analysis);
        var tank = fluidTank.get().getTankInventory();
        if (tank.isEmpty())
            return AugmentingContext.withAnalysis(AugmentingStatus.EMPTY_TANK, fluidTank, analysis);
        if (tank.getFluid().getFluid() != CEIAXFluids.APOTHEOTIC_ESSENCE.get())
            return AugmentingContext.withAnalysis(AugmentingStatus.WRONG_FLUID, fluidTank, analysis);
        int cost = analysis.result().map(AffixAugmenting.Result::cost).orElse(0);
        if (cost <= 0)
            return AugmentingContext.withAnalysis(AugmentingStatus.NO_UPGRADEABLE_AFFIXES, fluidTank, analysis);
        if (cost > tank.getCapacity())
            return AugmentingContext.withAnalysis(AugmentingStatus.TANK_TOO_SMALL, fluidTank, analysis);
        if (cost > tank.getFluidAmount())
            return AugmentingContext.withAnalysis(AugmentingStatus.INSUFFICIENT_ESSENCE, fluidTank, analysis);
        return AugmentingContext.withAnalysis(AugmentingStatus.READY, fluidTank, analysis);
    }

    private boolean canPay(Optional<FluidTankBlockEntity> fluidTank, int cost) {
        if (cost <= 0 || fluidTank.isEmpty())
            return false;
        var tank = fluidTank.get().getTankInventory();
        return tank.getFluid().getFluid() == CEIAXFluids.APOTHEOTIC_ESSENCE.get()
                && cost <= tank.getCapacity()
                && cost <= tank.getFluidAmount();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -1, 0);
    }

    private void spawnParticles() {
        ParticleOptions data = ParticleTypes.ENCHANT;
        Vec3 center = VecHelper.getCenterOf(worldPosition).add(0, -3.5 / 16f, 0);
        for (int i = 0; i < 3; i++) {
            var c = VecHelper.offsetRandomly(center, level.random, 1 / 16f);
            level.addParticle(data, c.x, center.y, c.z, 0, 0, 0);
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putBoolean("Powered", powered);
        if (activeAugmenting != null) {
            tag.putString("ActiveAffix", activeAugmenting.affixId().toString());
            tag.putFloat("ActiveFromLevel", activeAugmenting.fromLevel());
            tag.putFloat("ActiveToLevel", activeAugmenting.toLevel());
            tag.putInt("ActiveCost", activeAugmenting.cost());
        }
        if (clientPacket && heldPreview != null) {
            tag.putString("HeldPreviewStatus", heldPreview.status().name());
            tag.putString("HeldPreviewAffix", heldPreview.affixId().toString());
            tag.putFloat("HeldPreviewFromLevel", heldPreview.fromLevel());
            tag.putFloat("HeldPreviewToLevel", heldPreview.toLevel());
            tag.putInt("HeldPreviewCost", heldPreview.cost());
            tag.putInt("HeldPreviewTicks", heldPreviewTicks);
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        processingTicks = tag.contains("ProcessingTicks") ? tag.getInt("ProcessingTicks") : -1;
        if (processingTicks == 0)
            processingTicks = -1;
        powered = tag.getBoolean("Powered");
        activeAugmenting = null;
        heldInputTicks = 0;
        if (processingTicks > 0 && tag.contains("ActiveAffix")) {
            ResourceLocation affixId = ResourceLocation.tryParse(tag.getString("ActiveAffix"));
            float fromLevel = tag.getFloat("ActiveFromLevel");
            float toLevel = tag.getFloat("ActiveToLevel");
            int cost = tag.getInt("ActiveCost");
            if (affixId != null && toLevel > fromLevel + AffixOperationCosts.EPSILON && cost > 0) {
                activeAugmenting = new ActiveAugmenting(affixId, fromLevel, toLevel, cost);
                heldInputTicks = HELD_INPUT_TIMEOUT;
            }
        }
        heldPreview = null;
        heldPreviewTicks = 0;
        if (clientPacket && tag.contains("HeldPreviewStatus") && tag.contains("HeldPreviewAffix")) {
            AugmentingStatus status = augmentingStatusByName(tag.getString("HeldPreviewStatus"));
            ResourceLocation affixId = ResourceLocation.tryParse(tag.getString("HeldPreviewAffix"));
            float fromLevel = tag.getFloat("HeldPreviewFromLevel");
            float toLevel = tag.getFloat("HeldPreviewToLevel");
            int cost = tag.getInt("HeldPreviewCost");
            int ticks = tag.getInt("HeldPreviewTicks");
            if (status != null && affixId != null && toLevel > fromLevel + AffixOperationCosts.EPSILON && cost > 0 && ticks > 0) {
                heldPreview = new AugmentingPreview(status, affixId, fromLevel, toLevel, cost);
                heldPreviewTicks = ticks;
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!Apotheosis.enableAdventure)
            return false;
        CEILang.translate("gui.goggles.affix_augmentor").forGoggles(tooltip);
        addTankTooltip(tooltip);
        if (processingTicks > 0) {
            int progress = Math.round((UNIT_PROCESSING_TIME - processingTicks) * 100F / UNIT_PROCESSING_TIME);
            CEILang.translate("gui.goggles.affix_augmentor.processing", CEILang.number(progress).text("%").component())
                    .style(ChatFormatting.GREEN)
                    .forGoggles(tooltip);
            addActiveAugmentingTooltip(tooltip);
        } else {
            var input = getDepotInputStack();
            if (input.isPresent())
                addInputTooltip(tooltip, input.get(), isPlayerSneaking);
            else if (heldPreview != null)
                addHeldPreviewTooltip(tooltip, heldPreview);
            else
                CEILang.translate("gui.goggles.affix_augmentor.waiting")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip);
        }
        return true;
    }

    private void addActiveAugmentingTooltip(List<Component> tooltip) {
        if (activeAugmenting == null)
            return;
        CEILang.translate("gui.goggles.affix_augmentor.result")
                .forGoggles(tooltip);
        var input = getDepotInputStack();
        addAugmentingLine(tooltip, input, activeAugmenting, ChatFormatting.GREEN);
        CEILang.translate("gui.goggles.affix_augmentor.cost", amount(activeAugmenting.cost()).component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private void addHeldPreviewTooltip(List<Component> tooltip, AugmentingPreview preview) {
        CEILang.translate("gui.goggles.affix_augmentor.result")
                .forGoggles(tooltip);
        addAugmentingLine(tooltip, Optional.empty(), preview.toActiveAugmenting(), ChatFormatting.GREEN);
        CEILang.translate("gui.goggles.affix_augmentor.cost", amount(preview.cost()).component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        addStatusTooltip(tooltip, preview.status(), Optional.empty(), preview.cost(), Optional.empty());
    }

    private Optional<ItemStack> getDepotInputStack() {
        return getDepotBlockEntity().flatMap(this::getDepotInputStack);
    }

    private Optional<DepotBlockEntity> getDepotBlockEntity() {
        assert level != null;
        var be = level.getBlockEntity(worldPosition.below());
        if (be instanceof DepotBlockEntity depot)
            return Optional.of(depot);
        return Optional.empty();
    }

    private Optional<ItemStack> getDepotInputStack(DepotBlockEntity depot) {
        var stack = depot.getHeldItem();
        if (!stack.isEmpty())
            return Optional.of(stack);
        return Optional.empty();
    }

    private void addInputTooltip(List<Component> tooltip, ItemStack stack, boolean isPlayerSneaking) {
        var context = getAugmentingContext(stack);
        var result = context.analysis().result();
        if (result.isPresent()) {
            addResultTooltip(tooltip, stack, result.get(), ChatFormatting.GREEN);
            addRejectedAffixesTooltip(tooltip, context.analysis(), isPlayerSneaking);
        }
        if (context.status() == AugmentingStatus.READY) {
            return;
        }
        if (addStatusTooltip(tooltip, context.status(), context.tank(), result.map(AffixAugmenting.Result::cost).orElse(0), Optional.of(context.analysis())))
            return;
    }

    private boolean addStatusTooltip(
            List<Component> tooltip,
            AugmentingStatus status,
            Optional<FluidTankBlockEntity> tank,
            int cost,
            Optional<AffixAugmenting.Analysis> analysis) {
        if (status == AugmentingStatus.INSUFFICIENT_ESSENCE) {
            int available = tank
                    .map(fluidTank -> fluidTank.getTankInventory().getFluidAmount())
                    .orElseGet(() -> getExternalFluidTank()
                            .map(fluidTank -> fluidTank.getTankInventory().getFluidAmount())
                            .orElse(0));
            CEILang.translate(
                    "gui.goggles.affix_augmentor.insufficient_essence",
                    amount(available).component(),
                    amount(cost).component())
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return true;
        }
        if (status == AugmentingStatus.TANK_TOO_SMALL) {
            CEILang.translate("gui.goggles.affix_augmentor.tank_too_small", amount(cost).component())
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return true;
        }
        if (status == AugmentingStatus.MISSING_TANK
                || status == AugmentingStatus.EMPTY_TANK
                || status == AugmentingStatus.WRONG_FLUID)
            return true;
        switch (status) {
            case EMPTY_INPUT -> CEILang.translate("gui.goggles.affix_augmentor.waiting")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            case NO_AFFIXES -> CEILang.translate("gui.goggles.affix_augmentor.no_affixes")
                    .style(ChatFormatting.YELLOW)
                    .forGoggles(tooltip);
            case NO_UPGRADEABLE_AFFIXES -> {
                CEILang.translate("gui.goggles.affix_augmentor.no_upgradeable_affixes")
                        .style(ChatFormatting.YELLOW)
                        .forGoggles(tooltip);
                analysis.ifPresent(value -> addRejectedAffixesTooltip(tooltip, value, true));
            }
            default -> CEILang.translate("gui.goggles.affix_augmentor.waiting")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
        }
        return true;
    }

    private void addResultTooltip(List<Component> tooltip, ItemStack stack, AffixAugmenting.Result result, ChatFormatting style) {
        CEILang.translate("gui.goggles.affix_augmentor.result")
                .forGoggles(tooltip);
        CEILang.builder()
                .add(AffixTemplateDisplay.describeEquipmentAffixUpgrade(stack, result.target().affix(), result.currentLevel(), result.resultLevel()))
                .style(style)
                .forGoggles(tooltip, 1);
        CEILang.translate("gui.goggles.affix_augmentor.cost", amount(result.cost()).component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private void addAugmentingLine(List<Component> tooltip, Optional<ItemStack> input, ActiveAugmenting active, ChatFormatting style) {
        var affix = active.resolveAffix();
        if (input.isPresent() && affix.isPresent()) {
            CEILang.builder()
                    .add(AffixTemplateDisplay.describeEquipmentAffixUpgrade(input.get(), affix.get(), active.fromLevel(), active.toLevel()))
                    .style(style)
                    .forGoggles(tooltip, 1);
        } else if (affix.isPresent()) {
            CEILang.builder()
                    .add(affix.get().get().getName(true))
                    .text(" ")
                    .text(AffixTemplateDisplay.formatLevel(active.fromLevel()))
                    .text(" -> ")
                    .text(AffixTemplateDisplay.formatLevel(active.toLevel()))
                    .style(style)
                    .forGoggles(tooltip, 1);
        } else {
            CEILang.builder()
                    .text(active.affixId().toString())
                    .text(" ")
                    .text(AffixTemplateDisplay.formatLevel(active.fromLevel()))
                    .text(" -> ")
                    .text(AffixTemplateDisplay.formatLevel(active.toLevel()))
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        }
    }

    private void addRejectedAffixesTooltip(List<Component> tooltip, AffixAugmenting.Analysis analysis, boolean show) {
        if (!show || analysis.rejectedAffixes().isEmpty())
            return;
        CEILang.translate("gui.goggles.affix_augmentor.skipped")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        analysis.rejectedAffixes().stream()
                .limit(5)
                .forEach(rejected -> CEILang.translate(
                        "gui.goggles.affix_augmentor.rejected",
                        AffixTemplateDisplay.describeAffix(rejected.instance()),
                        rejectedReason(rejected))
                        .style(ChatFormatting.DARK_GRAY)
                        .forGoggles(tooltip, 1));
        int hidden = analysis.rejectedAffixes().size() - 5;
        if (hidden > 0) {
            CEILang.translate("gui.goggles.affix_augmentor.skipped.more", hidden)
                    .style(ChatFormatting.DARK_GRAY)
                    .forGoggles(tooltip, 1);
        }
    }

    private Component rejectedReason(AffixAugmenting.RejectedAffix rejected) {
        return switch (rejected.reason()) {
            case INVALID -> CEILang.translate("gui.goggles.affix_augmentor.rejection.invalid").component();
            case LEVEL_INDEPENDENT -> CEILang.translate("gui.goggles.affix_augmentor.rejection.level_independent").component();
            case AT_AUGMENTOR_CAP -> CEILang.translate(
                    "gui.goggles.affix_augmentor.rejection.at_cap",
                    AffixTemplateDisplay.formatLevel(rejected.maxLevel())).component();
            case DENIED_BY_RULE -> CEILang.translate("gui.goggles.affix_augmentor.rejection.denied_by_rule").component();
            case ZERO_COST -> CEILang.translate("gui.goggles.affix_augmentor.rejection.zero_cost").component();
        };
    }

    private void addTankTooltip(List<Component> tooltip) {
        var fluidTank = getExternalFluidTank();
        if (fluidTank.isEmpty()) {
            CEILang.translate("gui.goggles.affix_augmentor.missing_tank")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return;
        }
        var tank = fluidTank.get().getTankInventory();
        CEILang.translate("gui.goggles.affix_augmentor.tank")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        if (tank.isEmpty()) {
            CEILang.translate("gui.goggles.affix_augmentor.empty_tank")
                    .style(ChatFormatting.YELLOW)
                    .forGoggles(tooltip, 1);
            return;
        }
        var fluid = tank.getFluid();
        var amount = amount(tank.getFluidAmount(), tank.getCapacity());
        CEILang.builder()
                .add(fluid.getDisplayName())
                .text(" ")
                .add(amount)
                .style(fluid.getFluid() == CEIAXFluids.APOTHEOTIC_ESSENCE.get() ? ChatFormatting.GREEN : ChatFormatting.RED)
                .forGoggles(tooltip, 1);
        if (fluid.getFluid() != CEIAXFluids.APOTHEOTIC_ESSENCE.get()) {
            CEILang.translate("gui.goggles.affix_augmentor.wrong_fluid")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        }
    }

    private static LangBuilder amount(int amount) {
        return CEILang.number(amount).text(" mB");
    }

    private static LangBuilder amount(int amount, int capacity) {
        return amount(amount).text(" / ").add(amount(capacity));
    }

    @Nullable
    private static AugmentingStatus augmentingStatusByName(String name) {
        try {
            return AugmentingStatus.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static boolean hasUpgradableAffix(ItemStack stack) {
        return AffixAugmenting.canAugment(stack);
    }

    private enum AugmentingStatus {
        MISSING_TANK(false),
        EMPTY_TANK(false),
        WRONG_FLUID(false),
        EMPTY_INPUT(true),
        NO_AFFIXES(true),
        NO_UPGRADEABLE_AFFIXES(true),
        TANK_TOO_SMALL(false),
        INSUFFICIENT_ESSENCE(false),
        READY(false);

        private final boolean passesInput;

        AugmentingStatus(boolean passesInput) {
            this.passesInput = passesInput;
        }

        private boolean passesInput() {
            return passesInput;
        }
    }

    private record AugmentingContext(
            AugmentingStatus status,
            Optional<FluidTankBlockEntity> tank,
            AffixAugmenting.Analysis analysis) {
        private static AugmentingContext withAnalysis(
                AugmentingStatus status,
                Optional<FluidTankBlockEntity> tank,
                AffixAugmenting.Analysis analysis) {
            return new AugmentingContext(status, tank, analysis);
        }
    }

    private record ActiveAugmenting(ResourceLocation affixId, float fromLevel, float toLevel, int cost) {
        private static ActiveAugmenting from(AffixAugmenting.Result result) {
            return new ActiveAugmenting(
                    result.target().affix().getId(),
                    result.currentLevel(),
                    result.resultLevel(),
                    result.cost());
        }

        private Optional<DynamicHolder<Affix>> resolveAffix() {
            var affix = AffixRegistry.INSTANCE.holder(affixId);
            return affix.isBound() ? Optional.of(affix) : Optional.empty();
        }

        private boolean matchesInput(ItemStack stack) {
            return findAffix(stack).map(instance -> instance.isValid()
                    && Math.abs(instance.level() - fromLevel) <= AffixOperationCosts.EPSILON)
                    .orElse(false);
        }

        private Optional<AffixInstance> findAffix(ItemStack stack) {
            return AffixHelper.getAffixes(stack).entrySet().stream()
                    .filter(entry -> entry.getKey().getId().equals(affixId))
                    .map(Map.Entry::getValue)
                    .findFirst();
        }
    }

    private record AugmentingPreview(AugmentingStatus status, ResourceLocation affixId, float fromLevel, float toLevel, int cost) {
        private static AugmentingPreview from(AugmentingStatus status, AffixAugmenting.Result result) {
            return new AugmentingPreview(
                    status,
                    result.target().affix().getId(),
                    result.currentLevel(),
                    result.resultLevel(),
                    result.cost());
        }

        private ActiveAugmenting toActiveAugmenting() {
            return new ActiveAugmenting(affixId, fromLevel, toLevel, cost);
        }
    }
}
