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

package plus.dragons.createenchantmentindustry.config;

import net.createmod.catnip.config.ConfigBase;

public class CEIKineticsConfig extends ConfigBase {
    public final ConfigBool deployerKillDropXp = b(true, "deployerKillDropXp", Comments.deployerKillDropXp);
    public final ConfigFloat deployerKillXpScale = f(1, 0, 1, "deployerKillXpScale", Comments.deployerKillXpScale);
    public final ConfigBool deployerMineDropXp = b(true, "deployerMineDropXp", Comments.deployerMineDropXp);
    public final ConfigFloat deployerMineXpScale = f(1, 0, 1, "deployerMineXpScale", Comments.deployerMineXpScale);
    public final ConfigBool deployerCollectXp = b(true, "deployerCollectXp", Comments.deployerCollectXp);
    public final ConfigBool deployerMendItem = b(true, "deployerMendItem", Comments.deployerMendItem);
    public final ConfigBool deployerSweepAttack = b(true, "deployerSweepAttack", Comments.deployerSweepAttack);
    public final ConfigBool crushingWheelKillDropXp = b(true, "crushingWheelKillDropXp", Comments.crushingWheelKillDropXp);
    public final ConfigFloat crushingWheelKillDropXpChance = f(0.3f, 0, 1, "crushingWheelKillDropXpChance", Comments.crushingWheelKillDropXpChance);
    public final ConfigFloat crushingWheelKillDropXpScale = f(0.34f, 0, 1, "crushingWheelKillDropXpScale", Comments.crushingWheelKillDropXpScale);
    public final CEIStressConfig stressValues = nested(0, CEIStressConfig::new, Comments.stress);

    @Override
    public String getName() {
        return "kinetics";
    }

    static class Comments {
        static final String stress = "Fine tune the kinetic stats of individual components";
        static final String deployerKillDropXp = "Whether Deployer-killed entities should drop experience.";
        static final String deployerKillXpScale = "Scale for experience dropped from Deployer-killed entities.";
        static final String deployerMineDropXp = "Whether Deployer-mined blocks should drop experience.";
        static final String deployerMineXpScale = "Scale for experience dropped from Deployer-mined blocks.";
        static final String deployerCollectXp = "Whether Deployers collect dropped experience as Nuggets of Experience.";
        static final String deployerMendItem = "Whether the Mending enchantment applies to Deployer-held items (Needs deployerCollectXp = true).";
        static final String deployerSweepAttack = "Whether Deployers can perform sweep attacks.";
        static final String crushingWheelKillDropXp = "Whether Crushing Wheel-killed entities should drop experience.";
        static final String crushingWheelKillDropXpChance = "Probability of Crushing Wheel-killed entities dropping experience.";
        static final String crushingWheelKillDropXpScale = "Scale for experience dropped from Crushing Wheel-killed entities.";
    }
}
