package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiCreativeModeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnchantmentIndustry.ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = REGISTER.register("bingus",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("CEI"))
                    .withTabsBefore(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey(), AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
                    .icon(CeiItems.ENCHANTING_GUIDE::asStack)
                    .displayItems((params, output) -> {
                        output.accept(CeiBlocks.DISENCHANTER);
                        output.accept(CeiBlocks.PRINTER);
                        output.accept(CeiItems.ENCHANTING_GUIDE);
                        output.accept(CeiItems.EXPERIENCE_ROTOR);
                        output.accept(CeiFluids.INK.get().getBucket());
                        output.accept(CeiItems.HYPER_EXP_BOTTLE);
                    })
                    .build());

    public CeiCreativeModeTab() {}

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
