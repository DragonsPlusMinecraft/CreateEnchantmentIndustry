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

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import java.util.List;
import java.util.function.Consumer;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEILang;

@FieldsNullabilityUnknownByDefault
public class BlazeForgerBlockEntity extends BlazeExperienceBlockEntity {
    public static final int FORGING_TIME = 200;
    protected boolean special;
    protected boolean cursed;
    protected int processingTime = -1;
    protected final BlazeForgerInventory inventory;
    protected AdvancementBehaviour advancement;

    public BlazeForgerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new BlazeForgerInventory(this);
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == Direction.DOWN)
            return tanks.getCapability();
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.advancement = new AdvancementBehaviour(this);
        behaviours.add(this.advancement);
    }

    @Override
    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeForgerFluidCapacity.get(), fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.is(CEIFluids.EXPERIENCE));
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
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("ProcessingTime", processingTime);
        compound.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        processingTime = compound.getInt("ProcessingTime");
        inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
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
        boolean cursed = special && !worldPosition.equals(strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
            update = true;
        }
        if (level.isClientSide() && isVirtual()) {
            if (update) {
                inventory.updateResult();
                notifyUpdate();
            }
            var cost = inventory.getExperienceCost();
            if (cost > 0 && consumeExperience(cost, special, true)) {
                if (processingTime < 0) {
                    processingTime = FORGING_TIME / 4;
                    return;
                }
                if (processingTime > 0) {
                    processingTime--;
                    return;
                }
                consumeExperience(cost, special, false);
                processingTime = -1;
                inventory.applyResult();
            } else if (processingTime != -1) processingTime = -1;
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (update) {
            inventory.updateResult();
            notifyUpdate();
        }
        var cost = inventory.getExperienceCost();
        if (cost > 0 && consumeExperience(cost, special, true)) {
            if (processingTime < 0) {
                processingTime = FORGING_TIME;
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
            consumeExperience(cost, special, false);
            processingTime = -1;
            inventory.applyResult();
            notifyUpdate();
            level.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        } else if (processingTime != -1) {
            processingTime = -1;
            notifyUpdate();
        }
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (inventory.hasRemainingOutput()) return stack;
        if (!stack.isEmpty())
            stack = inventory.insertItem(0, stack, simulate);
        if (!stack.isEmpty())
            stack = inventory.insertItem(1, stack, simulate);
        return stack;
    }

    public ItemStack extractItem(boolean simulate) {
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack extracted = inventory.extractItem(i, 1, simulate);
            if (!extracted.isEmpty())
                return extracted;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        var style = special
                ? (cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        int cost = inventory.getExperienceCost();
        if (cost > 0) {
            added = true;
            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            CEILang.translate("gui.goggles.forging.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            for (int i = 0; i < 2; i++) {
                var result = inventory.getResult(i);
                if (result.isEmpty())
                    continue;
                CEILang.translate("gui.goggles.forging.result").forGoggles(tooltip);
                CEILang.item(result).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
                var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);
                if (!enchantments.isEmpty())
                    enchantments.addToTooltip(
                            TooltipContext.of(level),
                            component -> CEILang.builder().add(component).forGoggles(tooltip, 2),
                            TooltipFlag.NORMAL);
            }
        }
        return added;
    }
}
