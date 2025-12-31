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

package plus.dragons.createenchantmentindustry.integration.tlm;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/**
 * Handler for draining experience from Touhou Little Maid's EntityMaid.
 * This class is only loaded when TLM is present.
 */
public class MaidExperienceHandler {
    /**
     * Drain experience from nearby maids and fill into the fluid handler.
     *
     * @param level         the world level
     * @param effectiveAABB the area to search for maids
     * @param fluidHandler  the fluid handler to fill experience into
     * @return the total amount of experience drained
     */
    public static int drainMaidExperience(Level level, AABB effectiveAABB, IFluidHandler fluidHandler) {
        var rate = CEIConfig.fluids().experienceLanternDrainRate.get();
        List<EntityMaid> maids = level.getEntitiesOfClass(EntityMaid.class, effectiveAABB,
                maid -> maid.isAlive() && maid.getExperience() > 0);

        if (maids.isEmpty()) {
            return 0;
        }

        AtomicInteger totalDrained = new AtomicInteger();

        // Calculate the total experience we can drain
        AtomicInteger sum = new AtomicInteger();
        maids.forEach(maid -> {
            var maidExp = maid.getExperience();
            if (maidExp >= rate) {
                sum.addAndGet(rate);
            } else if (maidExp > 0) {
                sum.addAndGet(maidExp);
            }
        });

        if (sum.get() == 0) {
            return 0;
        }

        // Try to insert the experience fluid
        var inserted = fluidHandler.fill(new FluidStack(CEIFluids.EXPERIENCE, sum.get()), IFluidHandler.FluidAction.EXECUTE);

        if (inserted > 0) {
            // Distribute the drain across maids
            int remaining = inserted;
            for (var maid : maids) {
                if (remaining <= 0) break;

                var maidExp = maid.getExperience();
                int toDrain;

                if (remaining >= rate) {
                    toDrain = Math.min(maidExp, rate);
                } else {
                    toDrain = Math.min(maidExp, remaining);
                }

                if (toDrain > 0) {
                    maid.setExperience(maidExp - toDrain);
                    remaining -= toDrain;
                    totalDrained.addAndGet(toDrain);
                }
            }
        }

        return totalDrained.get();
    }
}
