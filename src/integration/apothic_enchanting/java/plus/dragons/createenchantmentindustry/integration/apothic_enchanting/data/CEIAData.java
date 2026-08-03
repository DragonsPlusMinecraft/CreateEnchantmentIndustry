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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.data;

import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import plus.dragons.createenchantmentindustry.data.CEINamedDataProvider;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.ponder.CEIAPonderPlugin;

public class CEIAData {
    public CEIAData(IEventBus modBus) {
        if (!DatagenModLoader.isRunningDataGen())
            return;
        REGISTRATE.registerPonderLocalization(CEIAPonderPlugin::new);
        modBus.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void generate(final GatherDataEvent event) {
        var generator = event.getGenerator();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();
        var output = generator.getPackOutput();
        var client = event.includeClient();
        var server = event.includeServer();
        generator.addProvider(server, new CEINamedDataProvider(
                "Create Enchantment Industry Apothic Enchanting Recipes", new CEIARecipeProvider(output)));
        generator.addProvider(server, new CEIAConditionalLootTableProvider(output, lookupProvider));
    }
}
