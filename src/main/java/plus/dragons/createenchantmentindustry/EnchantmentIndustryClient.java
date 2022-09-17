package plus.dragons.createenchantmentindustry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.ModPonderIndex;

public class EnchantmentIndustryClient {

    public static void onClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(EnchantmentIndustryClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        ModPonderIndex.register();
        ModPonderIndex.registerTags();
    }
}
