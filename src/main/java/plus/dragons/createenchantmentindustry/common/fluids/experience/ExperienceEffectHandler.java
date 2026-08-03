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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createdragonsplus.common.fluids.pipe.ConsumingOpenPipeEffectHandler;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

public class ExperienceEffectHandler implements ConsumingOpenPipeEffectHandler {
    @Override
    public int consume(Level level, AABB area, FluidStack contained) {
        if (!(level instanceof ServerLevel serverLevel))
            return 0;
        int amount = contained.getAmount();
        List<ServerPlayer> players = level.getEntitiesOfClass(
                ServerPlayer.class, area, player -> !(player instanceof FakePlayer));
        if (players.isEmpty()) {
            ExperienceOrb.award(serverLevel, area.getCenter(), ExperienceHelper.getExperienceFromFluid(contained));
        } else {
            ServerPlayer player = players.get(level.random.nextInt(players.size()));
            ExperienceHelper.award(amount, player);
            CEIAdvancements.A_SHOWER_EXPERIENCE.awardTo(player);
        }
        return amount;
    }

    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {}
}
