package plus.dragons.createenchantmentindustry;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.ink.InkRenderingCamera;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.ponder.CeiPonderPlugin;

public class EnchantmentIndustryClient {
    public EnchantmentIndustryClient() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        //Have to do this here because flywheel lied about the init timing ;(
        //Things won't work if you try init PartialModels in FMLClientSetupEvent
        CeiBlockPartials.register();
        modEventBus.register(this);
        registerForgeEvents(forgeEventBus);
    }

    private void registerForgeEvents(IEventBus forgeEventBus) {
        forgeEventBus.addListener(InkRenderingCamera::handleInkFogColor);
    }

    @SubscribeEvent
    public void setup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CeiPonderPlugin());
    }

    @SubscribeEvent
    public void loadComplete(final FMLLoadCompleteEvent event) {
        BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.ID, screen -> screen
                .withButtonLabels(null, null, "Gameplay Settings")
                .withSpecs(null, null, CeiConfigs.SERVER_SPEC));
    }
}
