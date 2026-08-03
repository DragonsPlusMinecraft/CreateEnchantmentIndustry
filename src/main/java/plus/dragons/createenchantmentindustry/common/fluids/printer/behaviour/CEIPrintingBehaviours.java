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

import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus.Internal;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@Internal
public final class CEIPrintingBehaviours {
    private CEIPrintingBehaviours() {}

    public static void register(IEventBus modBus) {
        register("package_address", enabled(
                () -> CEIConfig.fluids().enablePackageAddressPrinting.get(),
                AddressPrintingBehaviour::create));
        register("package_pattern", enabled(
                () -> CEIConfig.fluids().enablePackagePatternPrinting.get(),
                PackagePatternPrintingBehaviour::create));
        register("copy", enabled(
                () -> CEIConfig.fluids().enableCreateCopiableItemPrinting.get(),
                CopyPrintingBehaviour::create));
        register("custom_name", enabled(
                () -> CEIConfig.fluids().enableCustomNamePrinting.get(),
                CustomNamePrintingBehaviour::create));
        register("enchanted_book", enabled(
                () -> CEIConfig.fluids().enableEnchantedBookPrinting.get(),
                EnchantedBookPrintingBehaviour::create));
        register("written_book", enabled(
                () -> CEIConfig.fluids().enableWrittenBookPrinting.get(),
                WrittenBookPrintingBehaviour::create));
        register("banner_pattern", enabled(
                () -> CEIConfig.fluids().enableBannerPatternPrinting.get(),
                BannerPatternPrintingBehavior::create));
        PrintingBehaviourRegistry.register(modBus);
    }

    private static void register(String name, PrintingBehaviour.Provider provider) {
        PrintingBehaviourRegistry.registerBuiltin(name, () -> new PrintingBehaviourProvider(
                PrintingBehaviourProvider.BUILTIN_PRIORITY, provider));
    }

    private static PrintingBehaviour.Provider enabled(BooleanSupplier enabled, PrintingBehaviour.Provider provider) {
        return (level, tank, stack) -> enabled.getAsBoolean()
                ? provider.create(level, tank, stack)
                : Optional.empty();
    }
}
