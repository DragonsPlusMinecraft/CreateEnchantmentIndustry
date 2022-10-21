package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.InkRenderingCamera;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.foundation.config.ModConfigs;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;

public class EnchantmentIndustryClient {

    public static void onClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        //Have to do this here because flywheel lied about the init timing ;(
        //Things won't work if you try init PartialModels in FMLClientSetupEvent
        CeiBlockPartials.register();
        modEventBus.addListener(EnchantmentIndustryClient::clientInit);
        modEventBus.addListener(EnchantmentIndustryClient::loadComplete);
        forgeEventBus.addListener(InkRenderingCamera::handleInkFogColor);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CeiPonderIndex.register();
        CeiPonderIndex.registerTags();
        ModelBakery.UNREFERENCED_TEXTURES.add(BlazeEnchanterRenderer.BOOK_MATERIAL);
    }

    public static void loadComplete(final FMLLoadCompleteEvent event) {
        BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.MOD_ID, screen -> screen
            .withTitles(null, null, "Gameplay Settings")
            .withSpecs(null, null, ModConfigs.SERVER_SPEC)
        );
    }

}
