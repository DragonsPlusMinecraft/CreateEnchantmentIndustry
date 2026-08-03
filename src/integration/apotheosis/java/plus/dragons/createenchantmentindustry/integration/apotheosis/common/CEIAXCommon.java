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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import plus.dragons.createenchantmentindustry.data.CEINamedDataProvider;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apotheosis.client.ponder.CEIAXPonderPlugin;
import plus.dragons.createenchantmentindustry.integration.apotheosis.client.registry.CEIAXPartialModels;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.AffixComposingRules;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.*;
import plus.dragons.createenchantmentindustry.integration.apotheosis.config.CEIAXConfig;
import plus.dragons.createenchantmentindustry.integration.apotheosis.data.CEIAXConditionalLootTableProvider;
import plus.dragons.createenchantmentindustry.integration.apotheosis.data.CEIAXRecipeProvider;

public class CEIAXCommon {
    public CEIAXCommon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (ModIntegration.APOTHEOSIS.enabled()) {
            modBus.register(new Common(modBus, ModLoadingContext.get()));
            if (FMLLoader.getDist() == Dist.CLIENT)
                modBus.register(new Client());
        }
    }

    public static class Common {
        IEventBus modBus;
        ModLoadingContext modLoadingContext;

        Common(IEventBus modBus, ModLoadingContext modLoadingContext) {
            this.modBus = modBus;
            this.modLoadingContext = modLoadingContext;
        }

        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            CEIAXItems.register();
            CEIAXBlocks.register(modBus);
            CEIAXBlockEntities.register(modBus);
            CEIAXFluids.register(modBus);
            CEIAXCreativeModeTabs.register(modBus);
            CEIAXRecipes.register(modBus);
            CEIAXItemAttributes.register(modBus);
            CEIAXFanProcessingTypes.register(modBus);
            CEIAXArmInteractionPoints.register(modBus);
            CEIAXStats.register(modBus);
            modBus.register(new CEIAXConfig(modLoadingContext));
            MinecraftForge.EVENT_BUS.addListener(Common::addReloadListeners);
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
                    "Create Enchantment Industry Apotheosis Recipes", new CEIAXRecipeProvider(output)));
            generator.addProvider(server, new CEIAXConditionalLootTableProvider(output, lookupProvider));
        }

        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(AffixComposingRules.INSTANCE);
        }
    }

    public static class Client {
        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            CEIAXPartialModels.register();
            CEIAXPonderPlugin.register();
        }
    }
}
