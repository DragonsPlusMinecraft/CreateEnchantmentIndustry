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

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import java.util.List;
import java.util.function.Consumer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
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
import plus.dragons.createenchantmentindustry.common.migration.LegacyBlockEntityData;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.BlazeLightningHelper;

@FieldsNullabilityUnknownByDefault
public class BlazeEnchanterBlockEntity extends BlazeExperienceBlockEntity implements Clearable {
    public static final int ENCHANTING_TIME = 200;
    protected EnchanterBehaviour enchanter;
    protected boolean special;
    protected boolean cursed;
    protected Long seed;
    protected int processingTime = -1;
    protected ItemStack heldItem = ItemStack.EMPTY;
    protected AdvancementBehaviour advancement;
    protected @Nullable ActiveEnchanting activeEnchanting;

    public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.enchanter = new EnchanterBehaviour(this, new EnchanterTransform(), new TemplateItemTransform());
        this.advancement = new AdvancementBehaviour(this);
        behaviours.add(this.enchanter);
        behaviours.add(this.advancement);
    }

    @Override
    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeEnchanterFluidCapacity.get(), fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.getFluid() == CEIFluids.EXPERIENCE.get());
    }

    @Override
    protected ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeEnchanterFluidCapacity.get(), fluidUpdateCallback)
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
                ? CEIPartialModels.BLAZE_ENCHANTER_HAT
                : CEIPartialModels.BLAZE_ENCHANTER_HAT_SMALL;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (seed == null) {
            nextSeed();
            setChanged();
        }
        enchanter.update(heldItem);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem);
        }
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (seed != null)
            compound.putLong("Seed", seed);
        compound.putInt("ProcessingTime", processingTime);
        compound.put("HeldItem", heldItem.save(new CompoundTag()));
        if (activeEnchanting != null)
            compound.put("ActiveEnchanting", activeEnchanting.save());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        compound = LegacyBlockEntityData.migrateBlazeEnchanter(compound);
        super.read(compound, clientPacket);
        if (compound.contains("Seed", Tag.TAG_LONG))
            seed = compound.getLong("Seed");
        processingTime = compound.getInt("ProcessingTime");
        heldItem = ItemStack.of(compound.getCompound("HeldItem"));
        activeEnchanting = compound.contains("ActiveEnchanting", Tag.TAG_COMPOUND)
                ? ActiveEnchanting.load(compound.getCompound("ActiveEnchanting"))
                : null;
        if (processingTime >= 0 && activeEnchanting == null)
            processingTime = -1;
        updateEnchanterIfLevelReady();
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
            if (update) enchanter.update(heldItem);
            tickVirtual();
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (update) {
            enchanter.update(heldItem);
        }
        if (heldItem.isEmpty()) {
            cancelProcessing();
            return;
        }
        if (activeEnchanting == null) {
            if (enchanter.canProcess(heldItem))
                startProcessing(ENCHANTING_TIME, true);
            else if (processingTime != -1)
                cancelProcessing();
            return;
        }
        ActiveEnchanting active = activeEnchanting;
        if (!active.matches(heldItem)) {
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
        heldItem = active.result().copy();
        advancement.awardStat(CEIStats.ENCHANT.get(), 1);
        if (heldItem.getItem() instanceof EnchantingTemplateItem) {
            advancement.trigger(CEIAdvancements.SIGIL_FORGING.builtinTrigger());
        } else {
            advancement.trigger(CEIAdvancements.BLAZING_ENCHANTMENT.builtinTrigger());
        }
        if (active.special()) {
            advancement.awardStat(CEIStats.SUPER_ENCHANT.get(), 1);
            boolean treasure = CEIItemData.getEnchantmentsForCrafting(heldItem).keySet().stream()
                    .anyMatch(net.minecraft.world.item.enchantment.Enchantment::isTreasureOnly);
            if (treasure)
                advancement.trigger(CEIAdvancements.PROBABILITY_SPIKE.builtinTrigger());
        }
        finishProcessing();
        nextSeed();
        enchanter.update(heldItem);
        notifyUpdate();
        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    private void tickVirtual() {
        if (heldItem.isEmpty()) {
            cancelProcessing();
            return;
        }
        if (activeEnchanting == null) {
            if (enchanter.canProcess(heldItem))
                startProcessing(ENCHANTING_TIME / 4, false);
            return;
        }
        ActiveEnchanting active = activeEnchanting;
        if (!active.matches(heldItem)) {
            cancelProcessing();
            return;
        }
        if (processingTime > 0) {
            processingTime--;
            return;
        }
        heldItem = active.result().copy();
        finishProcessing();
        nextSeed();
        enchanter.update(heldItem);
    }

    private boolean startProcessing(int duration, boolean requireExperience) {
        int cost = enchanter.getExperienceCost();
        if (cost <= 0 || requireExperience && !consumeExperience(cost, special, true))
            return false;
        ItemStack input = heldItem.copy();
        ItemStack result = enchanter.getResult(input);
        if (result.isEmpty() || ItemStack.isSameItemSameTags(input, result) && input.getCount() == result.getCount())
            return false;
        activeEnchanting = new ActiveEnchanting(
                input,
                result.copy(),
                cost,
                special,
                special && !cursed);
        processingTime = duration;
        notifyUpdate();
        return true;
    }

    private void finishProcessing() {
        processingTime = -1;
        activeEnchanting = null;
    }

    private void cancelProcessing() {
        if (processingTime != -1 || activeEnchanting != null) {
            finishProcessing();
            notifyUpdate();
        }
    }

    public RandomSource getRandom() {
        return RandomSource.create(seed != null ? seed : worldPosition.asLong());
    }

    public void nextSeed() {
        assert level != null;
        seed = level.random.nextLong();
    }

    private void updateEnchanterIfLevelReady() {
        if (level != null)
            enchanter.update(heldItem);
    }

    public int getMaxEnchantLevel() {
        return getMaxEnchantLevel(getHeatLevel() == HeatLevel.SEETHING);
    }

    public int getMaxEnchantLevel(boolean special) {
        int max = CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel.get();
        int maxSuper = CEIConfig.enchantments().blazeEnchanterMaxSuperEnchantLevel.get();
        return special ? Math.max(max, maxSuper) : Mth.clamp(max, 0, maxSuper);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        assert level != null;
        if (!heldItem.isEmpty())
            return stack;
        var input = stack.copy();
        var inserted = input.split(1);
        enchanter.update(inserted);
        boolean canProcess = enchanter.canProcess(inserted);
        enchanter.update(heldItem);
        if (!canProcess) {
            return stack;
        }
        if (simulate)
            return input;
        heldItem = inserted;
        enchanter.update(heldItem);
        notifyUpdate();
        return input;
    }

    public ItemStack extractItem(boolean forced, boolean simulate) {
        assert level != null;
        ItemStack extracted = ItemStack.EMPTY;
        if (forced || activeEnchanting == null && processingTime <= 0) {
            extracted = heldItem.copy();
            if (!simulate) {
                heldItem = ItemStack.EMPTY;
                finishProcessing();
                enchanter.update(heldItem);
                notifyUpdate();
            }
        }
        return extracted;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        added |= enchanter.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return added;
    }

    @Override
    public void clearContent() {
        heldItem = ItemStack.EMPTY;
        finishProcessing();
        enchanter.update(heldItem);
    }

    protected record ActiveEnchanting(
            ItemStack input,
            ItemStack result,
            int cost,
            boolean special,
            boolean strikeLightning) {
        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("Input", input.save(new CompoundTag()));
            tag.put("Result", result.save(new CompoundTag()));
            tag.putInt("Cost", cost);
            tag.putBoolean("Special", special);
            tag.putBoolean("StrikeLightning", strikeLightning);
            return tag;
        }

        static @Nullable ActiveEnchanting load(CompoundTag tag) {
            ItemStack input = ItemStack.of(tag.getCompound("Input"));
            ItemStack result = ItemStack.of(tag.getCompound("Result"));
            int cost = tag.getInt("Cost");
            if (input.isEmpty() || result.isEmpty() || cost <= 0)
                return null;
            return new ActiveEnchanting(
                    input,
                    result,
                    cost,
                    tag.getBoolean("Special"),
                    tag.getBoolean("StrikeLightning"));
        }

        boolean matches(ItemStack stack) {
            return input.getCount() == stack.getCount() && ItemStack.isSameItemSameTags(input, stack);
        }
    }

    private static class EnchanterTransform extends ValueBoxTransform.Sided {
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

    private static class TemplateItemTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 14.5);
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack poseStack) {
            float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            TransformStack.of(poseStack).rotateYDegrees(yRot);
        }

        @Override
        public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            if (!isSideActive(state, getSide())) return false;
            Vec3 location = VecHelper.voxelSpace(8, 8, 13.5);
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            return localHit.distanceTo(location) < scale * 1.2;
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
