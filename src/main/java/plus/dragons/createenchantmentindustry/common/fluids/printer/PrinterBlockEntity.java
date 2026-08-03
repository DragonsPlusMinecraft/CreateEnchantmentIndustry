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
import java.util.List;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.AddressPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CustomNamePrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PackagePatternPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.migration.LegacyBlockEntityData;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@FieldsNullabilityUnknownByDefault
public class PrinterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public static final int PROCESSING_TIME = 50;
    private static final int COMPLETION_TICKS = 5;
    protected SmartFluidTankBehaviour tank;
    private PrinterBehaviour printer;
    public int processingTicks = -1;
    private AdvancementBehaviour advancement;
    private @Nullable ActivePrinting activePrinting;

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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER && tank != null && side != Direction.DOWN)
            return tank.getCapability().cast();
        return super.getCapability(capability, side);
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

        int requiredItem = printing.getRequiredItemCount(level, transported.stack);
        if (requiredItem <= 0 || transported.stack.getCount() < requiredItem)
            return PASS;

        var fluidStack = getFluidInTank();
        if (fluidStack.isEmpty())
            return HOLD;
        if (printing.getRequiredFluidAmount(level, transported.stack, fluidStack) <= 0)
            return PASS;

        return HOLD;
    }

    public ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        Level level = this.level;
        assert level != null;

        if (processingTicks > COMPLETION_TICKS) {
            if (activePrinting == null || !activePrinting.matchesInput(transported.stack)) {
                cancelProcessing();
                return startProcessing(transported);
            }
            return HOLD;
        }

        if (processingTicks == -1)
            return startProcessing(transported);

        ActivePrinting active = activePrinting;
        if (active == null) {
            cancelProcessing();
            return startProcessing(transported);
        }
        if (!active.matchesInput(transported.stack)) {
            cancelProcessing();
            return startProcessing(transported);
        }

        var fluidStack = getFluidInTank();
        if (!active.matchesFluid(fluidStack) || fluidStack.getAmount() < active.requiredFluidAmount())
            return HOLD;

        var completedPrinting = PrintingBehaviour.create(level, tank, active.template()).result();
        if (completedPrinting.isEmpty()) {
            cancelProcessing();
            return PASS;
        }

        transported.clearFanProcessingData();
        TransportedItemStack output = transported.copy();
        output.stack = active.result().copy();
        TransportedItemStack remains = null;
        if (transported.stack.getCount() > active.requiredItemCount()) {
            remains = transported.copy();
            remains.stack.shrink(active.requiredItemCount());
        }
        handler.handleProcessingOnItem(
                transported,
                TransportedResult.convertToAndLeaveHeld(List.of(output), remains));

        fluidStack.shrink(active.requiredFluidAmount());
        setFluidInTank(fluidStack);
        PrintingBehaviour printing = completedPrinting.get();
        printing.getResult(level, active.input().copy(), active.fluid().copy());
        printing.onFinished(level, worldPosition, this);
        awardPrintingAdvancements(active.result(), printing);
        advancement.awardStat(CEIStats.PRINT.get(), 1);
        finishProcessing();
        notifyUpdate();
        return HOLD;
    }

    private ProcessingResult startProcessing(TransportedItemStack transported) {
        Level level = this.level;
        assert level != null;

        var printing = printer.getPrintingBehaviour();
        if (!printing.isValid())
            return PASS;

        var requiredItem = printing.getRequiredItemCount(level, transported.stack);
        if (requiredItem <= 0 || transported.stack.getCount() < requiredItem)
            return PASS;

        var fluidStack = getFluidInTank();
        var requiredFluid = printing.getRequiredFluidAmount(level, transported.stack, fluidStack);
        if (requiredFluid <= 0)
            return PASS;
        if (fluidStack.getAmount() < requiredFluid)
            return HOLD;

        ItemStack input = transported.stack.copy();
        input.setCount(requiredItem);
        ItemStack resultItem = printing.getResult(level, input.copy(), fluidStack.copy());
        if (resultItem.isEmpty())
            return PASS;
        FluidStack fluidCost = fluidStack.copy();
        fluidCost.setAmount(requiredFluid);
        activePrinting = new ActivePrinting(
                input,
                printer.getFilter().copy(),
                fluidCost,
                resultItem.copy(),
                requiredItem,
                requiredFluid);
        processingTicks = PROCESSING_TIME;
        notifyUpdate();
        AllSoundEvents.SPOUTING.playOnServer(level, worldPosition, 0.75f, 0.9f + 0.2f * level.random.nextFloat());
        return HOLD;
    }

    private void awardPrintingAdvancements(ItemStack resultItem, PrintingBehaviour printing) {
        if (printing instanceof CustomNamePrintingBehaviour) advancement.trigger(CEIAdvancements.BRAND_REGISTRY.builtinTrigger());
        else if (resultItem.is(Items.WRITTEN_BOOK)) advancement.trigger(CEIAdvancements.COPIABLE_MASTERPIECE.builtinTrigger());
        else if (resultItem.is(Items.ENCHANTED_BOOK)) advancement.trigger(CEIAdvancements.COPIABLE_MYSTERY.builtinTrigger());
        else if (printing instanceof PackagePatternPrintingBehaviour) advancement.trigger(CEIAdvancements.ASSEMBLY_AESTHETICS.builtinTrigger());
        else if (printing instanceof AddressPrintingBehaviour) advancement.trigger(CEIAdvancements.SUPPLY_CHAIN_REFACTOR.builtinTrigger());
    }

    private void finishProcessing() {
        processingTicks = -1;
        activePrinting = null;
    }

    private void cancelProcessing() {
        if (processingTicks != -1 || activePrinting != null) {
            finishProcessing();
            notifyUpdate();
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("ProcessingTicks", processingTicks);
        if (activePrinting != null)
            tag.put("ActivePrinting", activePrinting.save());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        tag = LegacyBlockEntityData.migratePrinter(tag);
        super.read(tag, clientPacket);
        processingTicks = tag.getInt("ProcessingTicks");
        activePrinting = tag.contains("ActivePrinting", Tag.TAG_COMPOUND)
                ? ActivePrinting.load(tag.getCompound("ActivePrinting"))
                : null;
        if (processingTicks >= 0 && activePrinting == null)
            processingTicks = -1;
    }

    @Override
    public void tick() {
        super.tick();
        if (processingTicks > COMPLETION_TICKS) {
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
        boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
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

    private record ActivePrinting(
            ItemStack input,
            ItemStack template,
            FluidStack fluid,
            ItemStack result,
            int requiredItemCount,
            int requiredFluidAmount) {
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("Input", input.save(new CompoundTag()));
            tag.put("Template", template.save(new CompoundTag()));
            tag.put("Fluid", fluid.writeToNBT(new CompoundTag()));
            tag.put("Result", result.save(new CompoundTag()));
            tag.putInt("RequiredItemCount", requiredItemCount);
            tag.putInt("RequiredFluidAmount", requiredFluidAmount);
            return tag;
        }

        static @Nullable ActivePrinting load(CompoundTag tag) {
            ItemStack input = ItemStack.of(tag.getCompound("Input"));
            ItemStack template = ItemStack.of(tag.getCompound("Template"));
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
            ItemStack result = ItemStack.of(tag.getCompound("Result"));
            int requiredItemCount = tag.getInt("RequiredItemCount");
            int requiredFluidAmount = tag.getInt("RequiredFluidAmount");
            if (input.isEmpty()
                    || template.isEmpty()
                    || fluid.isEmpty()
                    || result.isEmpty()
                    || requiredItemCount <= 0
                    || requiredFluidAmount <= 0
                    || input.getCount() != requiredItemCount
                    || fluid.getAmount() != requiredFluidAmount)
                return null;
            return new ActivePrinting(
                    input,
                    template,
                    fluid,
                    result,
                    requiredItemCount,
                    requiredFluidAmount);
        }

        boolean matchesInput(ItemStack stack) {
            return stack.getCount() >= requiredItemCount && ItemStack.isSameItemSameTags(input, stack);
        }

        boolean matchesFluid(FluidStack stack) {
            return fluid.isFluidEqual(stack);
        }
    }
}
