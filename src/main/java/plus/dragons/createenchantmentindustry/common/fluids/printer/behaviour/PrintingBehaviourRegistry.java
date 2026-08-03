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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import com.mojang.serialization.DataResult;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import plus.dragons.createenchantmentindustry.api.registry.CEIRegistries;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/**
 * Forge registry and dispatcher for Printer behaviour providers.
 *
 * <p>Addons should register {@link PrintingBehaviourProvider} entries through a {@link DeferredRegister} created from
 * {@link CEIRegistries#PRINTING_BEHAVIOUR_PROVIDER}. Providers with a higher priority are queried first, while
 * providers with the same priority retain registry order. Recipe printing is always evaluated after every registered
 * provider.
 */
public final class PrintingBehaviourRegistry {
    private static final DeferredRegister<PrintingBehaviourProvider> PROVIDERS = DeferredRegister.create(CEIRegistries.PRINTING_BEHAVIOUR_PROVIDER, CEICommon.ID);
    public static final Supplier<IForgeRegistry<PrintingBehaviourProvider>> REGISTRY = PROVIDERS.makeRegistry(
            () -> new RegistryBuilder<PrintingBehaviourProvider>()
                    .disableSaving()
                    .disableSync()
                    .onBake(PrintingBehaviourRegistry::bake));
    private static volatile List<PrintingBehaviourProvider> sortedProviders;

    private PrintingBehaviourRegistry() {}

    static void registerBuiltin(String name, Supplier<PrintingBehaviourProvider> provider) {
        PROVIDERS.register(name, provider);
    }

    static void register(IEventBus modBus) {
        PROVIDERS.register(modBus);
    }

    private static void bake(
            IForgeRegistryInternal<PrintingBehaviourProvider> registry, RegistryManager registryManager) {
        var providers = new ArrayList<PrintingBehaviourProvider>();
        registry.forEach(providers::add);
        providers.sort(Comparator.comparingInt(PrintingBehaviourProvider::priority).reversed());
        sortedProviders = List.copyOf(providers);
    }

    static DataResult<PrintingBehaviour> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        var providers = sortedProviders;
        if (providers == null)
            throw new IllegalStateException("Printing behaviour registry has not finished registration");
        for (var entry : providers) {
            var result = Objects.requireNonNull(
                    entry.provider().create(level, tank, stack),
                    () -> "Printing behaviour provider " + REGISTRY.get().getKey(entry) + " returned null");
            if (result.isPresent())
                return result.get();
        }
        return DataResult.success(new RecipePrintingBehaviour(stack));
    }
}
