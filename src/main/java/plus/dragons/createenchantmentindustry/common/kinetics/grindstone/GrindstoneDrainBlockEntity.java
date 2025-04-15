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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import java.util.List;
import java.util.Optional;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;

@FieldsNullabilityUnknownByDefault
public class GrindstoneDrainBlockEntity extends KineticBlockEntity {
    public static final int GRINDING_TIME = 20;
    public ProcessingInventory inventory;
    private ItemStack processedItem = ItemStack.EMPTY;
    protected SmartFluidTankBehaviour tank;
    private DirectBeltInputBehaviour beltInput;
    private AdvancementBehaviour advancement;

    public GrindstoneDrainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ProcessingInventory(this::start) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                var space = tank.getPrimaryHandler().getSpace();
                if (GrindstoneHelper.getExperienceFromItem(stack) > space) return stack;
                if (GrindstoneHelper.getExperienceFromGrindingRecipe(level, stack) > space) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        }.withSlotLimit(true);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        tank = SmartFluidTankBehaviour.single(this, 1000);
        beltInput = new DirectBeltInputBehaviour(this).allowingBeltFunnels();
        advancement = new AdvancementBehaviour(this);
        behaviours.add(tank);
        behaviours.add(beltInput);
        behaviours.add(advancement);
    }

    public @Nullable IItemHandler getItemHandler(@Nullable Direction side) {
        if (side != Direction.DOWN)
            return inventory;
        return null;
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getOpposite())
            return tank.getCapability();
        return null;
    }

    private Direction getOutputSide() {
        var facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        var speed = facing == Direction.WEST || facing == Direction.NORTH ? getSpeed() * -1 : getSpeed();
        return speed > 0 ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public float getRelativeSpeed() {
        assert level != null;
        float speed = getSpeed();
        if (speed == 0f)
            return 0f;
        var above = worldPosition.above();
        var aboveState = level.getBlockState(above);
        if (!(aboveState.getBlock() instanceof MechanicalGrindstoneBlock grinderWheel))
            return 0f;
        var facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (grinderWheel.getRotationAxis(aboveState) != facing.getAxis())
            return 0f;
        float aboveSpeed = grinderWheel.getBlockEntityOptional(level, above)
                .map(KineticBlockEntity::getSpeed).orElse(0f);
        if (speed > 0f) {
            return aboveSpeed < 0f ? Math.min(speed, -aboveSpeed) : 0f;
        } else {
            return aboveSpeed > 0f ? Math.min(-speed, aboveSpeed) : 0f;
        }
    }

    private int getProcessDuration(ItemStack inputStack) {
        assert level != null;
        var recipeManager = level.getRecipeManager();
        var input = new SingleRecipeInput(inputStack);
        var sizeModifier = Math.max(1, (inputStack.getCount() / 5));
        var grinding = recipeManager.getRecipeFor(CEIRecipes.GRINDING.getType(), input, level);
        if (grinding.isPresent()) {
            return grinding.get().value().getProcessingDuration() * sizeModifier;
        }
        if (recipeManager.getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), input, level).isPresent()) {
            return 50 * sizeModifier;
        }
        if (GrindstoneHelper.canItemBeGrinded(level, inputStack, ItemStack.EMPTY)) {
            return 50 * sizeModifier;
        }
        return 10;
    }

    private boolean fill(FluidStack fluid) {
        if (tank.getPrimaryHandler().fill(fluid, FluidAction.SIMULATE) == fluid.getAmount()) {
            tank.getPrimaryHandler().fill(fluid, FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    private boolean drain(FluidIngredient fluidIngredient) {
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        int required = fluidIngredient.getRequiredAmount();
        if (fluidIngredient.test(fluid) && fluid.getAmount() >= required) {
            fluid.shrink(required);
            tank.getPrimaryHandler().setFluid(fluid);
            return true;
        }
        return false;
    }

    private void start(ItemStack inputStack) {
        assert level != null;
        if (inventory.isEmpty())
            return;
        if (level.isClientSide && !isVirtual())
            return;
        inventory.remainingTime = inventory.recipeDuration = getProcessDuration(inputStack);
        inventory.appliedRecipe = false;
        sendData();
    }

    private void applyRecipe() {
        assert level != null;
        var recipeManager = level.getRecipeManager();
        var inputStack = inventory.getStackInSlot(0);
        var input = new SingleRecipeInput(inputStack);
        // Grinding
        var grinding = SequencedAssemblyRecipe.getRecipe(level, input, CEIRecipes.GRINDING.getType(), GrindingRecipe.class);
        if (grinding.isEmpty())
            grinding = recipeManager.getRecipeFor(CEIRecipes.GRINDING.getType(), input, level);
        if (grinding.isPresent()) {
            var recipe = grinding.get().value();
            var fluidIngredients = recipe.getFluidIngredients();
            var fluidResults = recipe.getFluidResults();
            boolean applicable = true;
            if (!fluidIngredients.isEmpty())
                applicable = drain(fluidIngredients.getFirst());
            else if (!fluidResults.isEmpty())
                applicable = fill(fluidResults.getFirst());
            if (applicable) {
                if (fluidResults.getFirst().is(CEIFluids.EXPERIENCE))
                    advancement.awardStat(CEIStats.GRINDSTONE_EXPERIENCE.get(), fluidResults.getFirst().getAmount());
                inventory.clear();
                var grinded = recipe.rollResults();
                for (int i = 0; i < grinded.size(); i++)
                    inventory.setStackInSlot(i + 1, grinded.get(i));
                return;
            }
        }
        // Sand Paper Polishing
        Optional<RecipeHolder<SandPaperPolishingRecipe>> polishing = recipeManager
                .getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING.getType(), input, level);
        if (polishing.isPresent() && AllRecipeTypes.CAN_BE_AUTOMATED.test(polishing.get())) {
            var polished = polishing.get().value().getResultItem(level.registryAccess());
            advancement.trigger(CEIAdvancements.GRIND_TO_POLISH.builtinTrigger());
            inventory.clear();
            inventory.setStackInSlot(1, polished);
            return;
        }
        // Grind Stone
        var grindstone = GrindstoneHelper.grindItem(level, inputStack, ItemStack.EMPTY);
        if (grindstone.isPresent()) {
            var result = grindstone.get();
            var fluid = new FluidStack(CEIFluids.EXPERIENCE, result.experience());
            if (fill(fluid)) {
                advancement.trigger(CEIAdvancements.GONE_WITH_THE_FOIL.builtinTrigger());
                advancement.awardStat(CEIStats.GRINDSTONE_EXPERIENCE.get(), fluid.getAmount());
                inventory.clear();
                inventory.setStackInSlot(0, result.top());
                inventory.setStackInSlot(1, result.bottom());
                inventory.setStackInSlot(2, result.output());
            }
        }
    }

    private void spawnProcessedParticles(ItemStack stack) {
        assert level != null;
        if (stack.isEmpty())
            return;

        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem blockItem)
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState());
        else
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);

        Vec3 pos = Vec3.atBottomCenterOf(this.worldPosition).add(0, 1, 0);
        for (int i = 0; i < 10; i++) {
            Vec3 motion = VecHelper.offsetRandomly(new Vec3(0, 0.25f, 0), level.random, .125f);
            level.addParticle(particleData, pos.x, pos.y, pos.z, motion.x, motion.y, motion.y);
        }
    }

    private void spawnProcessingParticles(ItemStack stack) {
        assert level != null;
        if (stack.isEmpty())
            return;

        float speed;
        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem blockItem) {
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState());
            speed = 1f;
        } else {
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);
            speed = .125f;
        }

        Vec3 pos = Vec3.atBottomCenterOf(worldPosition).add(0, 1, 0);
        Direction inputSide = getOutputSide().getOpposite();
        float offset = inventory.recipeDuration != 0 ? inventory.remainingTime / inventory.recipeDuration : 0;
        offset /= 2;
        if (inventory.appliedRecipe)
            offset -= .5f;
        level.addParticle(particleData,
                pos.x + inputSide.getStepX() * offset,
                pos.y,
                pos.z + inputSide.getStepZ() * offset,
                inputSide.getStepX() * speed,
                level.random.nextFloat() * speed,
                inputSide.getStepZ() * speed);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(registries));
        if (clientPacket && !processedItem.isEmpty()) {
            compound.put("ProcessedItem", processedItem.saveOptional(registries));
            processedItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        if (compound.contains("ProcessedItem"))
            processedItem = ItemStack.parseOptional(registries, compound.getCompound("ProcessedItem"));
    }

    @Override
    public void destroy() {
        super.destroy();
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), processedItem);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        assert level != null;
        super.tickAudio();
        if (getSpeed() == 0)
            return;

        if (!processedItem.isEmpty()) {
            spawnProcessedParticles(processedItem);
            processedItem = ItemStack.EMPTY;
            level.levelEvent(1042, worldPosition, 0);
        }
    }

    @Override
    public void tick() {
        assert level != null;
        super.tick();

        float processingSpeed = getRelativeSpeed();
        if (processingSpeed == 0)
            return;
        if (inventory.remainingTime == -1) {
            if (!inventory.isEmpty() && !inventory.appliedRecipe)
                start(inventory.getStackInSlot(0));
            return;
        }

        processingSpeed = Mth.clamp(processingSpeed / 24, 1, 128);
        inventory.remainingTime -= processingSpeed;

        if (inventory.remainingTime > 0)
            spawnProcessingParticles(inventory.getStackInSlot(0));

        if (inventory.remainingTime < 5 && !inventory.appliedRecipe) {
            if (level.isClientSide && !isVirtual())
                return;
            processedItem = inventory.getStackInSlot(0);
            applyRecipe();
            inventory.appliedRecipe = true;
            inventory.recipeDuration = 20;
            inventory.remainingTime = 20;
            sendData();
            return;
        }

        Direction outputSide = getOutputSide();
        if (inventory.remainingTime > 0)
            return;
        inventory.remainingTime = 0;

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            ItemStack toFunnel = beltInput.tryExportingToBeltFunnel(stack, outputSide.getOpposite(), false);
            if (toFunnel != null) {
                if (toFunnel.getCount() != stack.getCount()) {
                    inventory.setStackInSlot(slot, toFunnel);
                    notifyUpdate();
                    return;
                }
                if (!toFunnel.isEmpty())
                    return;
            }
        }

        BlockPos outputPos = worldPosition.relative(outputSide);
        DirectBeltInputBehaviour outputTarget = BlockEntityBehaviour.get(level, outputPos, DirectBeltInputBehaviour.TYPE);
        if (outputTarget != null) {
            boolean changed = false;
            if (!outputTarget.canInsertFromSide(outputSide))
                return;
            if (level.isClientSide && !isVirtual())
                return;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty())
                    continue;
                ItemStack remainder = outputTarget.handleInsertion(stack, outputSide, false);
                if (ItemStack.matches(remainder, stack))
                    continue;
                inventory.setStackInSlot(slot, remainder);
                changed = true;
            }
            if (changed) {
                setChanged();
                sendData();
            }
            return;
        }

        Vec3 itemMovement = Vec3.atLowerCornerOf(outputSide.getNormal());
        Vec3 outPos = VecHelper.getCenterOf(worldPosition).add(itemMovement.scale(.5f).add(0, .5, 0));
        Vec3 outMotion = itemMovement.scale(.0625).add(0, .125, 0);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, stack);
            entityIn.setDeltaMovement(outMotion);
            level.addFreshEntity(entityIn);
        }
        inventory.clear();
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        inventory.remainingTime = -1;
        sendData();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        added |= this.containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
        return added;
    }
}
