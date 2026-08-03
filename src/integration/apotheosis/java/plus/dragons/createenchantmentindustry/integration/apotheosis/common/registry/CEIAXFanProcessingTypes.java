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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.fan.salvaging.SalvagingFanProcessingType;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

public class CEIAXFanProcessingTypes {
    private static final DeferredRegister<FanProcessingType> TYPES = DeferredRegister
            .create(CreateRegistries.FAN_PROCESSING_TYPE, CEIACommon.ID);
    public static final RegistryObject<SalvagingFanProcessingType> SALVAGING = TYPES
            .register("salvaging", SalvagingFanProcessingType::new);

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
