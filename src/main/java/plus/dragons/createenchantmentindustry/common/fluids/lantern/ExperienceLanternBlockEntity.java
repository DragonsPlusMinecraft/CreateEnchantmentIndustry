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

package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import static net.minecraft.world.level.block.DirectionalBlock.FACING;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ExperienceLanternBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    protected FluidTankBehaviour tank;
    protected AABB effectiveAABB;
    protected int rate;

    public ExperienceLanternBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        effectiveAABB = new AABB(getBlockPos()).inflate(0.5);
        rate = CEIConfig.fluids().experienceLanternDrainRate.get();
    }

    protected ConfigurableFluidTank createTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().experienceLanternFluidCapacity.get(), fluidUpdateCallback.andThen(this::onFluidStackChanged))
                .allowInsertion(fluidStack -> fluidStack.is(CEIFluids.EXPERIENCE));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && level.getGameTime() % 10 == 0) {
            drainExp();
        }
        if (!level.isClientSide && CEIConfig.fluids().experienceLanternPullToggle.get()) {
            pullExp();
        }
    }

    public FluidTankBehaviour getTank() {
        return tank;
    }

    protected void drainExp() {
        List<Player> players = level.getEntitiesOfClass(Player.class, effectiveAABB, player -> player.isAlive() && !player.isSpectator());
        if (!players.isEmpty()) {
            AtomicInteger sum = new AtomicInteger();
            players.forEach(player -> {
                var playerExp = ExperienceHelper.getExperienceForPlayer(player);
                if (playerExp >= rate) sum.addAndGet(rate);
                else if (playerExp != 0) sum.addAndGet(playerExp);
            });
            if (sum.get() != 0) {
                var inserted = tank.getPrimaryHandler().fill(new FluidStack(CEIFluids.EXPERIENCE, sum.get()), IFluidHandler.FluidAction.EXECUTE);
                if (inserted != 0) {
                    for (var player : players) {
                        var total = ExperienceHelper.getExperienceForPlayer(player);
                        if (inserted >= rate) {
                            if (total >= rate) {
                                player.giveExperiencePoints(-rate);
                                inserted -= rate;
                            } else if (total != 0) {
                                inserted -= total;
                                player.giveExperiencePoints(-total);
                            }
                        } else if (inserted > 0) {
                            if (total >= inserted) {
                                player.giveExperiencePoints(-inserted);
                                inserted = 0;
                            } else {
                                inserted -= total;
                                player.giveExperiencePoints(-total);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB);
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                var amount = orb.value;
                var fluidStack = new FluidStack(CEIFluids.EXPERIENCE.get(), amount);
                var inserted = tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                if (inserted == amount) {
                    orb.remove(Entity.RemovalReason.DISCARDED);
                } else {
                    if (inserted != 0) {
                        orb.value -= inserted;
                    }
                    break;
                }
            }
        }
    }

    protected void pullExp() {
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB.inflate(CEIConfig.fluids().experienceLanternPullRadius.get()));
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                if (orb.getDeltaMovement().length() <= .5) {
                    var pushForce = CEIConfig.fluids().experienceLanternPullForceMultiplier.get() * 1 / orb.position().distanceTo(getBlockPos().getCenter());
                    var directionToLantern = getBlockPos().getCenter().subtract(orb.position()).normalize().multiply(pushForce, pushForce, pushForce);
                    orb.push(directionToLantern);
                }
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new FluidTankBehaviour(this, this::createTank);
        behaviours.add(tank);
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        int light = ((int) (((float) tank.getPrimaryTank().tank.getFluid().getAmount() / tank.getPrimaryTank().tank.getCapacity()) * 15f));
        light = Math.min(Math.max(0, light), 15);
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(ExperienceLanternBlock.LIGHT, light));
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == null || side.getOpposite() == getBlockState().getValue(FACING))
            return tank.getCapability();
        return null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition, null));
    }
}
