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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import plus.dragons.createdragonsplus.common.CDPRegistrate;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.ponder.CEIAPonderPlugin;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.registry.CEIAPartialModels;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.contraptions.actors.enderWovenBag.EnderWovenBagItem;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlockEntity;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.*;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.config.CEIAConfig;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.data.CEIAData;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.integration.CEIMaxEnchantmentLevel;

public class CEIACommon {
    public static final String ID = CEICommon.ID;
    public static final CDPRegistrate REGISTRATE = CEICommon.REGISTRATE;

    public CEIACommon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (ModIntegration.APOTHIC_ENCHANTING.enabled()) {
            new CEIAData(modBus);
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
            CEIABlocks.register(modBus);
            CEIAItems.register();
            CEIABlockEntities.register(modBus);
            CEIAFluids.register(modBus);
            CEIACreativeModeTabs.register(modBus);
            CEIARecipes.register(modBus);
            CEIAItemAttributes.register(modBus);
            CEIAPackets.register();
            modBus.register(new CEIAConfig(modLoadingContext));
            MinecraftForge.EVENT_BUS.addListener(Common::addReloadListeners);
            MinecraftForge.EVENT_BUS.register(CEIAFluids.Events.class);
        }

        @SubscribeEvent
        public void setup(final FMLCommonSetupEvent event) {}

        @SubscribeEvent
        public void complete(final FMLLoadCompleteEvent event) {
            if (Apotheosis.enableEnch) {
                CEIMaxEnchantmentLevel.register();
            }
        }

        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(InfuserBlockEntity.RELOAD_LISTENER);
        }
    }

    public static class Client {
        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            // CEIPartialModels must be registered here,
            // or when PartialModelEventHandler#onRegisterAdditional triggered,
            // PartialModel.ALL won't include all partial model in 'some cases'
            // AllPartialModels#ini does not do this since AllPartialModels is already triggered at AllBlocks.TRACK
            // Issue: https://github.com/Creators-of-Create/Create/issues/8259
            CEIAPartialModels.register();
        }

        @SubscribeEvent
        public void setup(final FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                if (Apotheosis.enableEnch || Apotheosis.enableAdventure) {
                    PonderIndex.addPlugin(new CEIAPonderPlugin());
                }
                ItemProperties.register(
                        CEIABlocks.ENDER_WOVEN_BAG.asItem(),
                        CEICommon.asResource("open"),
                        EnderWovenBagItem::override);
            });
        }
    }
}
