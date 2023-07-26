package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnchantmentIndustry.ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("base",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Create Enchantment Industry"))
                    .withTabsBefore(AllCreativeModeTabs.MAIN_TAB.getId())
                    .icon(CeiItems.ENCHANTING_GUIDE::asStack)
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(CeiBlocks.DISENCHANTER);
                        pOutput.accept(CeiBlocks.PRINTER);
                        pOutput.accept(CeiItems.ENCHANTING_GUIDE);
                        pOutput.accept(CeiItems.EXPERIENCE_ROTOR);
                        pOutput.accept(CeiFluids.INK.get().getBucket());
                        pOutput.accept(CeiItems.HYPER_EXP_BOTTLE);
                    })
                    .build());
}
