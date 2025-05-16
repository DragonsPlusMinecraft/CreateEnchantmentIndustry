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

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ExperienceLanternMovementBehavior implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        var effectiveAABB = new AABB(context.position.subtract(0.5d, 0.5d, 0.5d), context.position.add(0.5d, 0.5d, 0.5d)).inflate(0.5);
        if (!context.world.isClientSide && context.world.getGameTime() % 10 == 0) {
            drainExp(context.world,
                    effectiveAABB,
                    context.contraption.getStorage().getFluids());
        }
        if (!context.world.isClientSide && CEIConfig.fluids().experienceLanternPullToggle.get()) {
            pullExp(context.world,
                    effectiveAABB,
                    context.position);
        }
    }

    protected void drainExp(Level level, AABB effectiveAABB, MountedFluidStorageWrapper tank) {
        var rate = CEIConfig.fluids().experienceLanternDrainRate.get();
        List<Player> players = level.getEntitiesOfClass(Player.class, effectiveAABB, player -> player.isAlive() && !player.isSpectator());
        if (!players.isEmpty()) {
            AtomicInteger sum = new AtomicInteger();
            players.forEach(player -> {
                var playerExp = ExperienceHelper.getExperienceForPlayer(player);
                if (playerExp >= rate) sum.addAndGet(rate);
                else if (playerExp != 0) sum.addAndGet(playerExp);
            });
            if (sum.get() != 0) {
                var inserted = tank.fill(new FluidStack(CEIFluids.EXPERIENCE, sum.get()), IFluidHandler.FluidAction.EXECUTE);
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
                var inserted = tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
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

    protected void pullExp(Level level, AABB effectiveAABB, Vec3 position) {
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB.inflate(CEIConfig.fluids().experienceLanternPullRadius.get()));
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                if (orb.getDeltaMovement().length() <= .5) {
                    var pushForce = CEIConfig.fluids().experienceLanternPullForceMultiplier.get() * 1 / orb.position().distanceTo(position);
                    var directionToLantern = position.subtract(orb.position()).normalize().multiply(pushForce, pushForce, pushForce);
                    orb.push(directionToLantern);
                }
            }
        }
    }
}
