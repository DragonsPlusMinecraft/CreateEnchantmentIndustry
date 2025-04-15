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
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.advancements.AdvancementBehaviour;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@FieldsNullabilityUnknownByDefault
public class BlazeEnchanterBlockEntity extends BlazeExperienceBlockEntity {
    public static final int ENCHANTING_TIME = 200;
    protected EnchanterBehaviour enchanter;
    protected boolean special;
    protected boolean cursed;
    protected Long seed;
    protected int processingTime = -1;
    protected ItemStack heldItem = ItemStack.EMPTY;
    protected AdvancementBehaviour advancement;

    public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == Direction.DOWN)
            return tanks.getCapability();
        return null;
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
                .allowInsertion(fluidStack -> fluidStack.is(CEIFluids.EXPERIENCE));
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
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem);
        }
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (seed != null)
            compound.putLong("Seed", seed);
        compound.putInt("ProcessingTime", processingTime);
        compound.put("HeldItem", heldItem.saveOptional(registries));
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (compound.contains("Seed", Tag.TAG_LONG))
            seed = compound.getLong("Seed");
        processingTime = compound.getInt("ProcessingTime");
        heldItem = ItemStack.parseOptional(registries, compound.getCompound("HeldItem"));
        super.read(compound, registries, clientPacket);
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
        boolean cursed = special && !worldPosition.equals(strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
            update = true;
        }
        if (level.isClientSide() && isVirtual()) {
            if (update) enchanter.update(heldItem);
            if (enchanter.canProcess(heldItem)) {
                if (processingTime < 0) {
                    processingTime = ENCHANTING_TIME / 4;
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    return;
                }
                processingTime = -1;
                heldItem = enchanter.getResult(heldItem);
                return;
            }
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (update) {
            enchanter.update(heldItem);
        }
        if (enchanter.canProcess(heldItem)) {
            var cost = enchanter.getExperienceCost();
            if (cost > 0 && consumeExperience(cost, special, true)) {
                if (processingTime < 0) {
                    processingTime = ENCHANTING_TIME;
                    notifyUpdate();
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    notifyUpdate();
                    return;
                }
                if (special && !cursed && strikeLightning(serverLevel, strikePos)) {
                    advancement.trigger(CEIAdvancements.OSHA_VIOLATION.builtinTrigger());
                    serverLevel.destroyBlock(worldPosition, false);
                    serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
                    return;
                }
                processingTime = -1;
                heldItem = enchanter.getResult(heldItem);
                advancement.awardStat(CEIStats.ENCHANT.get(), 1);

                if (heldItem.getItem() instanceof EnchantingTemplateItem) {
                    advancement.trigger(CEIAdvancements.SIGIL_FORGING.builtinTrigger());
                } else {
                    advancement.trigger(CEIAdvancements.BLAZING_ENCHANTMENT.builtinTrigger());
                }
                if (special) {
                    advancement.awardStat(CEIStats.SUPER_ENCHANT.get(), 1);
                    boolean treasure = EnchantmentHelper.getEnchantmentsForCrafting(heldItem).keySet().stream().anyMatch(h -> h.is(EnchantmentTags.TREASURE));
                    if (treasure)
                        advancement.trigger(CEIAdvancements.PROBABILITY_SPIKE.builtinTrigger());
                }

                consumeExperience(cost, special, false);
                nextSeed();
                notifyUpdate();
                level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
            } else {
                if (processingTime != -1) {
                    processingTime = -1;
                    notifyUpdate();
                }
            }
        } else if (processingTime != -1) {
            processingTime = -1;
            notifyUpdate();
        }
    }

    public RandomSource getRandom() {
        return RandomSource.create(seed);
    }

    public void nextSeed() {
        assert level != null;
        seed = level.random.nextLong();
    }

    public int getMaxEnchantLevel() {
        return getMaxEnchantLevel(getHeatLevel() == HeatLevel.SEETHING);
    }

    public int getMaxEnchantLevel(boolean special) {
        int max = CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel.get();
        int maxSuper = CEIConfig.enchantments().blazeEnchanterMaxSuperEnchantLevel.get();
        return special ? Math.max(max, maxSuper) : Math.clamp(max, 0, maxSuper);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        assert level != null;
        if (!heldItem.isEmpty())
            return stack;
        var input = stack.copy();
        var inserted = input.split(1);
        enchanter.update(inserted);
        if (!enchanter.canProcess(inserted)) {
            enchanter.update(ItemStack.EMPTY);
            return stack;
        }
        if (simulate)
            return input;
        heldItem = inserted;
        notifyUpdate();
        return input;
    }

    public ItemStack extractItem(boolean forced, boolean simulate) {
        assert level != null;
        ItemStack extracted = ItemStack.EMPTY;
        if (forced || processingTime <= 0) {
            extracted = heldItem.copy();
            if (!simulate) {
                heldItem = ItemStack.EMPTY;
                processingTime = -1;
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
