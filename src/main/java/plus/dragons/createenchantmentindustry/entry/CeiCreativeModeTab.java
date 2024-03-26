package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiCreativeModeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER;
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB;

    public CeiCreativeModeTab() {}

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    static {
        REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnchantmentIndustry.ID);
        CREATIVE_TAB = REGISTER.register("base", () -> {
            return CreativeModeTab.builder().title(Components.literal("CEI"))
                    .withTabsBefore(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey(),AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
                    .icon(CeiItems.ENCHANTING_GUIDE::asStack)
                    .displayItems((params, output) -> {
                        output.accept(CeiBlocks.DISENCHANTER);
                        output.accept(CeiBlocks.PRINTER);
                        output.accept(CeiItems.ENCHANTING_GUIDE);
                        output.accept(CeiItems.EXPERIENCE_ROTOR);
                        output.accept(CeiFluids.INK.get().getBucket());
                        output.accept(CeiItems.HYPER_EXP_BOTTLE);
                    }).build();
        });
    }
}
