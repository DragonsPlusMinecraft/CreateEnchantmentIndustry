package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.TargetEnchantmentDisplaySource;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterDisplaySource;

import java.util.function.Supplier;

public class CeiDisplaySources {
    public static final RegistryEntry<DisplaySource, PrinterDisplaySource> COPY_CONTENT = simple("copy_content", PrinterDisplaySource::new);
    public static final RegistryEntry<DisplaySource, TargetEnchantmentDisplaySource> TARGET_ENCHANTMENT = simple("target_enchantment", TargetEnchantmentDisplaySource::new);


    public static void register() {
    }

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(String name, Supplier<T> supplier) {
        return EnchantmentIndustry.REGISTRATE.displaySource(name, supplier).register();
    }
}
