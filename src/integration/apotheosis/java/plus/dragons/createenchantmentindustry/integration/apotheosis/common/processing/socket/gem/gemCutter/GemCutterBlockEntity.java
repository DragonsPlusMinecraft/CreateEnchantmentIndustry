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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter;

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
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import java.util.*;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.belt.lowerProcessingAppliance.LowerBeltProcessingBehaviour;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.socket.gem.gemCutter.GemCutting.Tier;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class GemCutterBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    public static final int UNIT_PROCESSING_TIME = 200;
    public static final int COMPLETION_TICKS = 25;
    private static final int HELD_INPUT_TIMEOUT = 3;
    public int processingTicks = -1;
    public boolean powered;
    public float chargingPercentage;
    @Nullable
    private ActiveCutting activeCutting;
    @Nullable
    private CuttingPreview heldPreview;
    private int heldPreviewTicks;
    private int heldInputTicks;

    public GemCutterBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
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
                if (isCrystalEssence(tank.getFluid())) {
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
            if (!level.isClientSide && !hasValidActiveInput()) {
                cancelProcessing();
                return;
            }
            if (powered) {
                processingTicks--;
                if (level.isClientSide && processingTicks > 25) {
                    spawnParticles();
                }
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

        if (!isUpgradableGem(transported.stack))
            return PASS;

        return HOLD;
    }

    public BeltProcessingBehaviour.ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (!Apotheosis.enableAdventure)
            return PASS;
        Level level = this.level;
        assert level != null;
        var context = getCuttingContext(transported.stack);
        rememberHeldPreview(context);
        if (context.status() == CuttingStatus.ALREADY_PERFECT) {
            cancelProcessing();
            return PASS;
        }
        if (context.status() == CuttingStatus.NOT_A_GEM) {
            cancelProcessing();
            return PASS;
        }
        refreshHeldInput();

        if (processingTicks > COMPLETION_TICKS) {
            if (!validateTransportedInput(transported.stack))
                return PASS;
            return HOLD;
        }

        if (processingTicks == -1)
            return startProcessingIfReady(context, transported.stack);

        if (activeCutting == null) {
            if (context.status() != CuttingStatus.READY) {
                cancelProcessing();
                return HOLD;
            }
            activeCutting = ActiveCutting.from(context, transported.stack);
        }
        var active = activeCutting;
        if (!active.matchesInput(transported.stack)) {
            cancelProcessing();
            return isUpgradableGem(transported.stack) ? startProcessingIfReady(context, transported.stack) : PASS;
        }
        if (!canPay(context.tank(), active.cost())) {
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
        Optional<ItemStack> upgraded = GemCutting.upgradeOne(result.stack, active.from());
        if (upgraded.isEmpty()) {
            cancelProcessing();
            return PASS;
        }
        result.stack = upgraded.get();
        handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(List.of(result), remains));
        level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.AMETHYST_CLUSTER_HIT,
                SoundSource.BLOCKS, 0.75f, .9f + 0.2f * level.random.nextFloat());
        context.tank().get().getTankInventory().drain(active.cost(), IFluidHandler.FluidAction.EXECUTE);
        cancelProcessing();
        return HOLD;
    }

    private BeltProcessingBehaviour.ProcessingResult startProcessingIfReady(CuttingContext context, ItemStack input) {
        if (context.status() != CuttingStatus.READY)
            return HOLD;
        activeCutting = ActiveCutting.from(context, input);
        refreshHeldInput();
        processingTicks = UNIT_PROCESSING_TIME;
        notifyUpdate();
        return HOLD;
    }

    private void rememberHeldPreview(CuttingContext context) {
        if (context.status() == CuttingStatus.NOT_A_GEM || context.status() == CuttingStatus.ALREADY_PERFECT) {
            clearHeldPreview();
            return;
        }
        CuttingPreview preview = CuttingPreview.from(context);
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
        if (activeCutting == null)
            return isUpgradableGem(stack);
        if (!activeCutting.matchesInput(stack)) {
            cancelProcessing();
            return false;
        }
        return true;
    }

    private boolean canPay(Optional<FluidTankBlockEntity> fluidTank, int cost) {
        if (cost <= 0 || fluidTank.isEmpty())
            return false;
        var tank = fluidTank.get().getTankInventory();
        return isCrystalEssence(tank.getFluid())
                && tank.getFluidAmount() >= cost
                && tank.getCapacity() >= cost;
    }

    private boolean hasValidActiveInput() {
        if (processingTicks < 0)
            return true;
        var depot = getDepotBlockEntity();
        if (depot.isEmpty())
            return heldInputTicks > 0;
        if (activeCutting == null)
            return getDepotInputStack(depot.get()).map(GemCutterBlockEntity::isUpgradableGem).orElse(false);
        return getDepotInputStack(depot.get())
                .map(activeCutting::matchesInput)
                .orElse(false);
    }

    private void cancelProcessing() {
        if (processingTicks != -1 || activeCutting != null) {
            processingTicks = -1;
            activeCutting = null;
            heldInputTicks = 0;
            notifyUpdate();
        }
    }

    private CuttingContext getCuttingContext(ItemStack stack) {
        var fluidTank = getExternalFluidTank();
        Optional<Tier> tier = GemCutting.tier(stack);
        if (tier.isEmpty())
            return CuttingContext.withTank(CuttingStatus.NOT_A_GEM, fluidTank);
        var from = tier.get();
        if (!GemCutting.canCut(from))
            return CuttingContext.withGem(CuttingStatus.ALREADY_PERFECT, fluidTank, from, from, 0);
        var to = GemCutting.resultTier(from);
        int cost = GemCutting.getCutCost(from);
        if (fluidTank.isEmpty())
            return CuttingContext.withGem(CuttingStatus.MISSING_TANK, fluidTank, from, to, cost);
        var tank = fluidTank.get().getTankInventory();
        if (tank.isEmpty())
            return CuttingContext.withGem(CuttingStatus.EMPTY_TANK, fluidTank, from, to, cost);
        if (!isCrystalEssence(tank.getFluid()))
            return CuttingContext.withGem(CuttingStatus.WRONG_FLUID, fluidTank, from, to, cost);
        if (cost > tank.getCapacity())
            return CuttingContext.withGem(CuttingStatus.TANK_TOO_SMALL, fluidTank, from, to, cost);
        if (cost > tank.getFluidAmount())
            return CuttingContext.withGem(CuttingStatus.INSUFFICIENT_ESSENCE, fluidTank, from, to, cost);
        return CuttingContext.withGem(CuttingStatus.READY, fluidTank, from, to, cost);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -1, 0);
    }

    private void spawnParticles() {
        ParticleOptions data = ParticleTypes.FALLING_OBSIDIAN_TEAR;
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        for (int i = 0; i < 5; i++) {
            var c = VecHelper.offsetRandomly(center, level.random, 3 / 16f);
            level.addParticle(data, c.x, center.y, c.z, 0, -20, 0);
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putBoolean("Powered", powered);
        if (activeCutting != null) {
            tag.putString("ActiveFromRarity", activeCutting.from().name());
            tag.putString("ActiveToRarity", activeCutting.to().name());
            tag.putInt("ActiveCost", activeCutting.cost());
            tag.put("ActiveInput", activeCutting.input().save(new CompoundTag()));
        }
        if (clientPacket && heldPreview != null) {
            tag.putString("HeldPreviewStatus", heldPreview.status().name());
            tag.putString("HeldPreviewFromRarity", heldPreview.from().name());
            tag.putString("HeldPreviewToRarity", heldPreview.to().name());
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
        activeCutting = null;
        heldInputTicks = 0;
        String activeFromName = tag.contains("ActiveFromRarity") ? tag.getString("ActiveFromRarity") : tag.getString("ActiveFromPurity");
        String activeToName = tag.contains("ActiveToRarity") ? tag.getString("ActiveToRarity") : tag.getString("ActiveToPurity");
        if (processingTicks > 0 && !activeFromName.isEmpty() && !activeToName.isEmpty() && tag.contains("ActiveInput")) {
            Tier from = tierByName(activeFromName);
            Tier to = tierByName(activeToName);
            int cost = tag.getInt("ActiveCost");
            ItemStack input = ItemStack.of(tag.getCompound("ActiveInput"));
            if (from != null && to != null && from != to && cost > 0 && !input.isEmpty()) {
                activeCutting = new ActiveCutting(from, to, cost, input);
                heldInputTicks = HELD_INPUT_TIMEOUT;
            }
        }
        if (processingTicks > 0 && activeCutting == null)
            processingTicks = -1;
        heldPreview = null;
        heldPreviewTicks = 0;
        String previewFromName = tag.contains("HeldPreviewFromRarity") ? tag.getString("HeldPreviewFromRarity") : tag.getString("HeldPreviewFromPurity");
        String previewToName = tag.contains("HeldPreviewToRarity") ? tag.getString("HeldPreviewToRarity") : tag.getString("HeldPreviewToPurity");
        if (clientPacket && tag.contains("HeldPreviewStatus") && !previewFromName.isEmpty() && !previewToName.isEmpty()) {
            CuttingStatus status = cuttingStatusByName(tag.getString("HeldPreviewStatus"));
            Tier from = tierByName(previewFromName);
            Tier to = tierByName(previewToName);
            int cost = tag.getInt("HeldPreviewCost");
            int ticks = tag.getInt("HeldPreviewTicks");
            if (status != null && from != null && to != null && cost > 0 && ticks > 0) {
                heldPreview = new CuttingPreview(status, from, to, cost);
                heldPreviewTicks = ticks;
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!Apotheosis.enableAdventure)
            return false;
        CEILang.translate("gui.goggles.gem_cutter").forGoggles(tooltip);
        addTankTooltip(tooltip);
        if (processingTicks > 0) {
            int progress = Math.round((UNIT_PROCESSING_TIME - processingTicks) * 100F / UNIT_PROCESSING_TIME);
            CEILang.translate("gui.goggles.gem_cutter.processing", CEILang.number(progress).text("%").component())
                    .style(ChatFormatting.GREEN)
                    .forGoggles(tooltip);
            addActiveCuttingTooltip(tooltip);
        } else {
            var input = getDepotInputStack();
            if (input.isPresent())
                addInputTooltip(tooltip, input.get());
            else if (heldPreview != null)
                addHeldPreviewTooltip(tooltip, heldPreview);
            else
                CEILang.translate("gui.goggles.gem_cutter.waiting")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip);
        }
        return true;
    }

    private void addActiveCuttingTooltip(List<Component> tooltip) {
        if (activeCutting == null)
            return;
        addResultTooltip(tooltip, Optional.empty(), activeCutting.from(), activeCutting.to(), activeCutting.cost(), ChatFormatting.GREEN);
    }

    private void addHeldPreviewTooltip(List<Component> tooltip, CuttingPreview preview) {
        addResultTooltip(tooltip, Optional.empty(), preview.from(), preview.to(), preview.cost(), ChatFormatting.GREEN);
        addContextStatusTooltip(tooltip, preview.status(), Optional.empty(), preview.cost());
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

    private void addInputTooltip(List<Component> tooltip, ItemStack stack) {
        var context = getCuttingContext(stack);
        if (context.hasResult()) {
            addResultTooltip(tooltip, Optional.of(stack), context.from(), context.to(), context.cost(), ChatFormatting.GREEN);
        }
        if (context.status() == CuttingStatus.READY)
            return;
        if (addContextStatusTooltip(tooltip, context.status(), context.tank(), context.cost()))
            return;
        CEILang.translate("gui.goggles.gem_cutter.waiting")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
    }

    private boolean addContextStatusTooltip(List<Component> tooltip, CuttingStatus status, Optional<FluidTankBlockEntity> tank, int cost) {
        if (status == CuttingStatus.INSUFFICIENT_ESSENCE) {
            int available = tank
                    .map(fluidTank -> fluidTank.getTankInventory().getFluidAmount())
                    .orElseGet(() -> getExternalFluidTank()
                            .map(fluidTank -> fluidTank.getTankInventory().getFluidAmount())
                            .orElse(0));
            CEILang.translate(
                    "gui.goggles.gem_cutter.insufficient_essence",
                    amount(available).component(),
                    amount(cost).component())
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return true;
        }
        if (status == CuttingStatus.TANK_TOO_SMALL) {
            int capacity = tank
                    .map(fluidTank -> fluidTank.getTankInventory().getCapacity())
                    .orElseGet(() -> getExternalFluidTank()
                            .map(fluidTank -> fluidTank.getTankInventory().getCapacity())
                            .orElse(0));
            CEILang.translate("gui.goggles.gem_cutter.max_cost_tank_too_small", amount(cost, capacity).component())
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return true;
        }
        if (status == CuttingStatus.ALREADY_PERFECT) {
            CEILang.translate("gui.goggles.gem_cutter.already_perfect")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            return true;
        }
        if (status == CuttingStatus.NOT_A_GEM) {
            CEILang.translate("gui.goggles.gem_cutter.invalid_input")
                    .style(ChatFormatting.YELLOW)
                    .forGoggles(tooltip);
            return true;
        }
        if (status == CuttingStatus.MISSING_TANK || status == CuttingStatus.EMPTY_TANK || status == CuttingStatus.WRONG_FLUID)
            return true;
        return false;
    }

    private void addResultTooltip(List<Component> tooltip, Optional<ItemStack> input, Tier from, Tier to, int cost, ChatFormatting style) {
        CEILang.translate("gui.goggles.gem_cutter.result")
                .forGoggles(tooltip);
        resultLine(input, from, to)
                .style(style)
                .forGoggles(tooltip, 1);
        CEILang.translate("gui.goggles.gem_cutter.result_cost", amount(cost).component())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private LangBuilder resultLine(Optional<ItemStack> input, Tier from, Tier to) {
        if (input.isPresent()) {
            ItemStack output = GemCutting.upgradeOne(input.get(), from).orElse(ItemStack.EMPTY);
            return CEILang.translate(
                    "gui.goggles.gem_cutter.result_line",
                    output.isEmpty() ? input.get().getHoverName() : output.getHoverName(),
                    from.displayName(),
                    to.displayName());
        }
        return CEILang.translate(
                "gui.goggles.gem_cutter.result_purity",
                from.displayName(),
                to.displayName());
    }

    private void addTankTooltip(List<Component> tooltip) {
        var fluidTank = getExternalFluidTank();
        if (fluidTank.isEmpty()) {
            CEILang.translate("gui.goggles.gem_cutter.missing_tank")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return;
        }
        var tank = fluidTank.get().getTankInventory();
        CEILang.translate("gui.goggles.gem_cutter.tank")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        if (tank.isEmpty()) {
            CEILang.translate("gui.goggles.gem_cutter.empty_tank")
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
                .style(isCrystalEssence(fluid) ? ChatFormatting.GREEN : ChatFormatting.RED)
                .forGoggles(tooltip, 1);
        if (!isCrystalEssence(fluid)) {
            CEILang.translate("gui.goggles.gem_cutter.wrong_fluid")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        }
        int maxCost = maxCutCost();
        if (tank.getCapacity() < maxCost) {
            CEILang.translate("gui.goggles.gem_cutter.max_cost_tank_too_small", amount(maxCost, tank.getCapacity()).component())
                    .style(ChatFormatting.YELLOW)
                    .forGoggles(tooltip, 1);
        }
    }

    private static int maxCutCost() {
        int max = 0;
        for (var tier : Tier.values()) {
            if (GemCutting.canCut(tier))
                max = Math.max(max, GemCutting.getCutCost(tier));
        }
        return max;
    }

    private static LangBuilder amount(int amount) {
        return CEILang.number(amount).text(" mB");
    }

    private static LangBuilder amount(int amount, int capacity) {
        return amount(amount).text(" / ").add(amount(capacity));
    }

    public static boolean isUpgradableGem(ItemStack stack) {
        return Apotheosis.enableAdventure && stack.is(Adventure.Items.GEM.get()) && GemCutting.canCut(stack);
    }

    @Nullable
    private static Tier tierByName(String name) {
        try {
            return Tier.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return switch (name) {
                case "CRACKED" -> Tier.COMMON;
                case "CHIPPED" -> Tier.UNCOMMON;
                case "FLAWED" -> Tier.RARE;
                case "NORMAL" -> Tier.EPIC;
                case "FLAWLESS" -> Tier.MYTHIC;
                case "PERFECT" -> Tier.ANCIENT;
                default -> null;
            };
        }
    }

    private static boolean isCrystalEssence(FluidStack fluid) {
        return !fluid.isEmpty() && fluid.getFluid() == CEIAXFluids.CRYSTAL_ESSENCE.getSource();
    }

    @Nullable
    private static CuttingStatus cuttingStatusByName(String name) {
        try {
            return CuttingStatus.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private enum CuttingStatus {
        MISSING_TANK,
        EMPTY_TANK,
        WRONG_FLUID,
        NOT_A_GEM,
        ALREADY_PERFECT,
        TANK_TOO_SMALL,
        INSUFFICIENT_ESSENCE,
        READY
    }

    private record CuttingContext(
            CuttingStatus status,
            Optional<FluidTankBlockEntity> tank,
            Tier from,
            Tier to,
            int cost) {
        private boolean hasResult() {
            return cost > 0 && from != to;
        }

        private static CuttingContext withTank(CuttingStatus status, Optional<FluidTankBlockEntity> tank) {
            return new CuttingContext(status, tank, Tier.COMMON, Tier.COMMON, 0);
        }

        private static CuttingContext withGem(CuttingStatus status, Optional<FluidTankBlockEntity> tank, Tier from, Tier to, int cost) {
            return new CuttingContext(status, tank, from, to, cost);
        }
    }

    private record ActiveCutting(Tier from, Tier to, int cost, ItemStack input) {
        private ActiveCutting {
            input = input.copy();
            input.setCount(1);
        }

        private static ActiveCutting from(CuttingContext context, ItemStack input) {
            return new ActiveCutting(context.from(), context.to(), context.cost(), input);
        }

        private boolean matchesInput(ItemStack stack) {
            return !stack.isEmpty()
                    && ItemStack.isSameItemSameTags(stack, input)
                    && GemCutting.tier(stack).map(tier -> tier == from).orElse(false);
        }
    }

    private record CuttingPreview(CuttingStatus status, Tier from, Tier to, int cost) {
        private static CuttingPreview from(CuttingContext context) {
            return new CuttingPreview(context.status(), context.from(), context.to(), context.cost());
        }
    }
}
