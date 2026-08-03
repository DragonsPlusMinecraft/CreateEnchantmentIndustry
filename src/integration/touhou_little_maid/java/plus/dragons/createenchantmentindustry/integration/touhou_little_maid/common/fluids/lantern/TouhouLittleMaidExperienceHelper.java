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

package plus.dragons.createenchantmentindustry.integration.touhou_little_maid.common.fluids.lantern;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.integration.touhou_little_maid.config.CEITouhouLittleMaidConfig;

public class TouhouLittleMaidExperienceHelper {
    private TouhouLittleMaidExperienceHelper() {}

    public static int drainMaidExperience(Level level, AABB area, IFluidHandler tank) {
        var server = CEITouhouLittleMaidConfig.server();
        if (!server.experienceLanternDrainMaidExperience.get())
            return 0;
        int maxDrainPerMaid = server.experienceLanternMaxDrainPerMaid.get();
        int totalDrained = 0;
        for (var maid : level.getEntitiesOfClass(EntityMaid.class, area, maid -> maid.isAlive() && maid.getExperience() > 0)) {
            int drainable = Math.min(maid.getExperience(), maxDrainPerMaid);
            var simulatedStack = new FluidStack(CEIFluids.EXPERIENCE.get(), drainable);
            int accepted = tank.fill(simulatedStack, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0)
                break;
            int inserted = tank.fill(new FluidStack(CEIFluids.EXPERIENCE.get(), accepted), IFluidHandler.FluidAction.EXECUTE);
            if (inserted <= 0)
                break;
            maid.setExperience(maid.getExperience() - inserted);
            totalDrained += inserted;
            if (inserted < accepted)
                break;
        }
        return totalDrained;
    }
}
