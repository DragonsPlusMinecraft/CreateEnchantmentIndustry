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

package plus.dragons.createenchantmentindustry.integration.touhou_little_maid.common;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.touhou_little_maid.config.CEITouhouLittleMaidConfig;

public class CEITouhouLittleMaidCommon {
    public CEITouhouLittleMaidCommon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (ModIntegration.TOUHOU_LITTLE_MAID.enabled())
            modBus.register(new CEITouhouLittleMaidConfig(ModLoadingContext.get()));
    }
}
