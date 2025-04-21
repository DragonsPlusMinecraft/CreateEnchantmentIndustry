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

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.AddressPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CustomNamePrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PackagePatternPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@FieldsNullabilityUnknownByDefault
public class PrinterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public static final int PROCESSING_TIME = 50;
    protected SmartFluidTankBehaviour tank;
    private PrinterBehaviour printer;
    public int processingTicks = -1;
    private AdvancementBehaviour advancement;

    public PrinterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, CEIConfig.fluids().printerFluidCapacity.get());
        printer = new PrinterBehaviour(this, tank, new CenteredSideValueBoxTransform(
                (state, direction) -> state.getValue(PrinterBlock.FACING) == direction));
        BeltProcessingBehaviour processing = new BeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemEnters)
                .whileItemHeld(this::onItemHeld);
        advancement = new AdvancementBehaviour(this);
        behaviours.add(tank);
        behaviours.add(printer);
        behaviours.add(processing);
        behaviours.add(advancement);
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side != Direction.DOWN)
            return tank.getCapability();
        return null;
    }

    private FluidStack getFluidInTank() {
        return tank.getPrimaryHandler().getFluid();
    }

    private void setFluidInTank(FluidStack fluidStack) {
        tank.getPrimaryHandler().setFluid(fluidStack);
    }

    public ProcessingResult onItemEnters(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        Level level = this.level;
        assert level != null;

        if (handler.blockEntity.isVirtual())
            return PASS;

        var printing = printer.getPrintingBehaviour();
        if (!printing.isValid())
            return PASS;

        if (printing.getRequiredItemCount(level, transported.stack) == 0)
            return PASS;

        var fluidStack = getFluidInTank();
        if (fluidStack.isEmpty())
            return HOLD;
        if (printing.getRequiredFluidAmount(level, transported.stack, fluidStack) == 0)
            return PASS;

        return HOLD;
    }

    public ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        Level level = this.level;
        assert level != null;

        if (processingTicks != -1 && processingTicks != 5)
            return HOLD;

        var printing = printer.getPrintingBehaviour();
        if (!printing.isValid())
            return PASS;

        var requiredItem = printing.getRequiredItemCount(level, transported.stack);
        if (requiredItem == 0)
            return PASS;

        var fluidStack = getFluidInTank();
        var requiredFluid = printing.getRequiredFluidAmount(level, transported.stack, fluidStack);
        if (requiredFluid == 0)
            return PASS;
        if (fluidStack.getAmount() < requiredFluid)
            return HOLD;

        if (processingTicks == -1) {
            processingTicks = PROCESSING_TIME;
            notifyUpdate();
            AllSoundEvents.SPOUTING.playOnServer(level, worldPosition, 0.75f, 0.9f + 0.2f * level.random.nextFloat());
            return HOLD;
        }

        ItemStack resultItem = printing.getResult(level, transported.stack.split(requiredItem), fluidStack);
        if (!resultItem.isEmpty()) {
            transported.clearFanProcessingData();
            TransportedItemStack held = null;
            TransportedItemStack result = transported.copy();
            result.stack = resultItem;
            if (!transported.stack.isEmpty())
                held = transported.copy();
            List<TransportedItemStack> resultList = new ArrayList<>();
            resultList.add(result);
            handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(resultList, held));
            if (printer.getPrintingBehaviour() instanceof CustomNamePrintingBehaviour) advancement.trigger(CEIAdvancements.BRAND_REGISTRY.builtinTrigger());
            else if (resultItem.is(Items.WRITTEN_BOOK)) advancement.trigger(CEIAdvancements.COPIABLE_MASTERPIECE.builtinTrigger());
            else if (resultItem.is(Items.ENCHANTED_BOOK)) advancement.trigger(CEIAdvancements.COPIABLE_MYSTERY.builtinTrigger());
            else if (printer.getPrintingBehaviour() instanceof PackagePatternPrintingBehaviour) advancement.trigger(CEIAdvancements.ASSEMBLY_AESTHETICS.builtinTrigger());
            else if (printer.getPrintingBehaviour() instanceof AddressPrintingBehaviour) advancement.trigger(CEIAdvancements.SUPPLY_CHAIN_REFACTOR.builtinTrigger());
        }
        fluidStack.shrink(requiredFluid);
        setFluidInTank(fluidStack);
        notifyUpdate();
        printing.onFinished(level, worldPosition, this);
        advancement.awardStat(CEIStats.PRINT.get(), 1);
        return HOLD;
    }

    @Override
    protected void write(CompoundTag tag, Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("ProcessingTicks", processingTicks);
    }

    @Override
    protected void read(CompoundTag tag, Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        processingTicks = tag.getInt("ProcessingTicks");
    }

    @Override
    public void tick() {
        super.tick();
        if (processingTicks >= 0) {
            processingTicks--;
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        assert level != null;
        boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, tank.getPrimaryHandler());
        added |= printer.getPrintingBehaviour().addToGoggleTooltip(tooltip, isPlayerSneaking);
        return added;
    }

    protected static class PrinterFilterSlot extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(PrinterBlock.FACING) == direction;
        }
    }
}
