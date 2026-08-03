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

package plus.dragons.createenchantmentindustry.integration.apotheosis.config;

import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class CEIAXConfig {
    private static final CEIAXClientConfig CLIENT_CONFIG = new CEIAXClientConfig();
    private static final CEIAXServerConfig SERVER_CONFIG = new CEIAXServerConfig();
    private static ForgeConfigSpec CLIENT_SPEC;
    private static ForgeConfigSpec SERVER_SPEC;

    public CEIAXConfig(ModLoadingContext modLoadingContext) {
        CLIENT_SPEC = Util.make(new ForgeConfigSpec.Builder().configure(builder -> {
            CLIENT_CONFIG.registerAll(builder);
            return Unit.INSTANCE;
        }).getValue(), spec -> modLoadingContext.registerConfig(Type.CLIENT, spec, "create_enchantment_industry-apotheosis-client.toml"));
        SERVER_SPEC = Util.make(new ForgeConfigSpec.Builder().configure(builder -> {
            SERVER_CONFIG.registerAll(builder);
            return Unit.INSTANCE;
        }).getValue(), spec -> modLoadingContext.registerConfig(Type.SERVER, spec, "create_enchantment_industry-apotheosis-server.toml"));
    }

    public static CEIAXClientConfig client() {
        return CLIENT_CONFIG;
    }

    public static CEIAXServerConfig server() {
        return SERVER_CONFIG;
    }

    @SubscribeEvent
    public void onLoad(ModConfigEvent.Loading event) {
        var spec = event.getConfig().getSpec();
        if (SERVER_SPEC == spec) {
            SERVER_CONFIG.onLoad();
        } else if (CLIENT_SPEC == spec) {
            CLIENT_CONFIG.onLoad();
        }
    }

    @SubscribeEvent
    public void onReload(ModConfigEvent.Reloading event) {
        var spec = event.getConfig().getSpec();
        if (SERVER_SPEC == spec) {
            SERVER_CONFIG.onReload();
        } else if (CLIENT_SPEC == spec) {
            CLIENT_CONFIG.onReload();
        }
    }
}
