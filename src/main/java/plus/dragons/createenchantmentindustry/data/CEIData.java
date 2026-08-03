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

package plus.dragons.createenchantmentindustry.data;

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderPlugin;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

public class CEIData {
    public CEIData(IEventBus modBus) {
        if (!DatagenModLoader.isRunningDataGen())
            return;
        REGISTRATE.registerBuiltinLocalization("interface");
        REGISTRATE.registerForeignLocalization();
        REGISTRATE.registerPonderLocalization(CEIPonderPlugin::new);
        REGISTRATE.registerExtraLocalization(CEIAdvancements::provideLang);
        modBus.register(this);
    }

    @SubscribeEvent
    public void generate(final GatherDataEvent event) {
        var generator = event.getGenerator();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();
        var output = generator.getPackOutput();
        var client = event.includeClient();
        var server = event.includeServer();

        CEIGenerateEntriesProvider generatedEntriesProvider = new CEIGenerateEntriesProvider(output, lookupProvider);
        lookupProvider = generatedEntriesProvider.getRegistryProvider();

        generator.addProvider(event.includeServer(), generatedEntriesProvider);
        generator.addProvider(server, new CEIDataMapProvider(output));
        generator.addProvider(server, new CEIRecipeProvider(output));
        generator.addProvider(server, new CEIAdvancements(output, lookupProvider));
        generator.addProvider(server, new CEIEnchantmentTagsProvider(output, lookupProvider, existingFileHelper));
    }
}
