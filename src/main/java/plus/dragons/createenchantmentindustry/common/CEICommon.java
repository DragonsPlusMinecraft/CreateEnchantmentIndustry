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

package plus.dragons.createenchantmentindustry.common;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import java.util.Set;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.slf4j.Logger;
import plus.dragons.createdragonsplus.common.CDPRegistrate;
import plus.dragons.createenchantmentindustry.client.CEIClient;
import plus.dragons.createenchantmentindustry.common.crafting.CEIApotheosisModuleCondition;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CEIPrintingBehaviours;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRules;
import plus.dragons.createenchantmentindustry.common.registry.*;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.data.CEIData;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;

@Mod(CEICommon.ID)
public class CEICommon {
    public static final String ID = "create_enchantment_industry";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CDPRegistrate REGISTRATE = new CDPRegistrate(ID)
            .setTooltipModifier(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create(item))));

    public CEICommon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CEIApotheosisModuleCondition.register();
        REGISTRATE.registerEventListeners(modBus);
        CEIFluids.register(modBus);
        CEIBlocks.register(modBus);
        CEIBlockEntities.register(modBus);
        CEIItems.register(modBus);
        CEICreativeModeTabs.register(modBus);
        CEIRecipes.register(modBus);
        CEIEnchantments.register(modBus);
        CEIArmInterationPoints.register(modBus);
        CEIDataMaps.register(modBus);
        CEIStats.register(modBus);
        CEIMountedStorageTypes.register(modBus);
        CEIItemAttributes.register(modBus);
        CEIPrintingBehaviours.register(modBus);
        new CEIData(modBus);
        initializeIntegration(
                ModIntegration.APOTHIC_ENCHANTING,
                "plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon");
        initializeIntegration(
                ModIntegration.APOTHEOSIS,
                "plus.dragons.createenchantmentindustry.integration.apotheosis.common.CEIAXCommon");
        initializeIntegration(
                ModIntegration.TOUHOU_LITTLE_MAID,
                "plus.dragons.createenchantmentindustry.integration.touhou_little_maid.common.CEITouhouLittleMaidCommon");
        modBus.register(this);
        modBus.register(new CEIConfig(ModLoadingContext.get()));
        MinecraftForge.EVENT_BUS.addListener(CEICommon::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(CEICommon::missingMappings);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CEIClient::new);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CEIAdvancements.register();
            CEIAdvancements.BuiltinTriggersQuickDeploy.register();
        });
    }

    public static void serverStarted(final ServerStartedEvent event) {
        EnchantmentProcessingRules.warnLegacyDataMaps(event.getServer());
    }

    private static void missingMappings(MissingMappingsEvent event) {
        warnRemovedMappings(event, Registries.BLOCK, Set.of("disenchanter"));
        warnRemovedMappings(event, Registries.BLOCK_ENTITY_TYPE, Set.of("disenchanter"));
        warnRemovedMappings(
                event,
                Registries.ITEM,
                Set.of("disenchanter", "enchanting_guide", "hyper_experience_bottle", "experience_rotor", "ink_bucket"));
        warnRemovedMappings(
                event,
                Registries.FLUID,
                Set.of("hyper_experience", "flowing_hyper_experience", "ink", "flowing_ink"));
    }

    private static <T> void warnRemovedMappings(
            MissingMappingsEvent event, ResourceKey<? extends Registry<T>> registry, Set<String> removedPaths) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(registry, ID)) {
            if (!removedPaths.contains(mapping.getKey().getPath()))
                continue;
            mapping.warn();
            LOGGER.warn(
                    "Legacy registry entry {} was removed in 2.5 and has no semantics-preserving replacement; it will not be remapped",
                    mapping.getKey());
        }
    }

    private static void initializeIntegration(ModIntegration integration, String className) {
        if (!integration.enabled())
            return;
        try {
            Class.forName(className, true, CEICommon.class.getClassLoader()).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException ignored) {
            LOGGER.debug("{} integration source set is not present", integration.id());
        } catch (ReflectiveOperationException | LinkageError exception) {
            throw new IllegalStateException("Failed to initialize " + integration.id() + " integration", exception);
        }
    }

    public static ResourceLocation asResource(String name) {
        return ResourceLocation.fromNamespaceAndPath(ID, name);
    }

    public static String asLocalization(String key) {
        return ID + "." + key;
    }
}
