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

package plus.dragons.createenchantmentindustry.client;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import java.util.function.Consumer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderPlugin;
import plus.dragons.createenchantmentindustry.common.processing.BlazeCustomRenderedBlockItem.Renderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterItemRenderer;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterItemRenderer;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerItemRenderer;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;

public class CEIClient {
    public CEIClient() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // CEIPartialModels must be registered here,
        // or when PartialModelEventHandler#onRegisterAdditional triggered,
        // PartialModel.ALL won't include all partial model in 'some cases'
        // AllPartialModels#ini does not do this since AllPartialModels is already triggered at AllBlocks.TRACK
        // Issue: https://github.com/Creators-of-Create/Create/issues/8259
        CEIPartialModels.register();
        modBus.addListener(CEIClient::setup);
        MinecraftForge.EVENT_BUS.addListener(CEIClient::logout);
    }

    public static void setup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CEIPonderPlugin());
    }

    private static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        CEIDataMaps.clearClientSnapshot();
    }

    public static void initializeBlazeItemRenderer(
            Item item, Renderer renderer, Consumer<IClientItemExtensions> consumer) {
        var itemRenderer = switch (renderer) {
            case ENCHANTER -> new BlazeEnchanterItemRenderer();
            case FORGER -> new BlazeForgerItemRenderer();
            case CLASSIC_ENCHANTER -> new ClassicBlazeEnchanterItemRenderer();
        };
        consumer.accept(SimpleCustomRenderer.create(item, itemRenderer));
    }
}
