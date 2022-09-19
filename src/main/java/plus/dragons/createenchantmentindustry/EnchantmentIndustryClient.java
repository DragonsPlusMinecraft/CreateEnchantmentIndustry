package plus.dragons.createenchantmentindustry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import plus.dragons.createenchantmentindustry.entry.ModBlockPartials;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.ModPonderIndex;

public class EnchantmentIndustryClient {

    public static void onClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(EnchantmentIndustryClient::clientInit);
        //Have to do this here because flywheel lied about the init timing ;(
        //Things won't work if you try init PartialModels in FMLClientSetupEvent
        ModBlockPartials.register();
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        ModPonderIndex.register();
        ModPonderIndex.registerTags();
    }
}
