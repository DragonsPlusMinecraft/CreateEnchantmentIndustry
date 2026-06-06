package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;
import java.util.function.Supplier;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.TargetEnchantmentDisplaySource;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterDisplaySource;

public class CeiDisplaySources {
    public static final RegistryEntry<PrinterDisplaySource> COPY_CONTENT = simple("copy_content", PrinterDisplaySource::new);
    public static final RegistryEntry<TargetEnchantmentDisplaySource> TARGET_ENCHANTMENT = simple("target_enchantment", TargetEnchantmentDisplaySource::new);

    public static void register() {}

    private static <T extends DisplaySource> RegistryEntry<T> simple(String name, Supplier<T> supplier) {
        return EnchantmentIndustry.REGISTRATE.displaySource(name, supplier).register();
    }
}
