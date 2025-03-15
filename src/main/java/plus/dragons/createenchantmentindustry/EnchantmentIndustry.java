package plus.dragons.createenchantmentindustry;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.AdvancementFactory;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.init.SafeRegistrate;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.lang.Lang;
//import plus.dragons.createenchantmentindustry.compat.apotheosis.ApotheosisCompat;
//import plus.dragons.createenchantmentindustry.compat.quark.QuarkCompat;
import plus.dragons.createenchantmentindustry.entry.*;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

@Mod(EnchantmentIndustry.ID)
public class EnchantmentIndustry {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String NAME = "Create: Enchantment Industry";
    public static final String ID = "create_enchantment_industry";
    public static final SafeRegistrate REGISTRATE = new SafeRegistrate(ID);
    public static final Lang LANG = new Lang(ID);
    public static final AdvancementFactory ADVANCEMENT_FACTORY = AdvancementFactory.create(NAME, ID,
        CeiAdvancements::register);

    public EnchantmentIndustry(IEventBus modEventBus) {
        IEventBus forgeEventBus = NeoForge.EVENT_BUS;
        
        CeiConfigs.register(ModLoadingContext.get().getActiveContainer());
        
        registerEntries(modEventBus);
        modEventBus.register(this);
        registerForgeEvents(forgeEventBus);
    }

    private void registerEntries(IEventBus modEventBus) {
        CeiBlocks.register();
        CeiBlockEntities.register();
        modEventBus.addListener(CeiBlockEntities::registerCapabilities);
        CeiContainerTypes.register();
        CeiEntityTypes.register();
        CeiFluids.register();
        CeiComponents.register(modEventBus);
        CeiItems.register();
        CeiRecipeTypes.register(modEventBus);
        CeiTags.register();
        CeiCreativeModeTab.register(modEventBus);
        CeiDisplaySources.register();
        REGISTRATE.registerEventListeners(modEventBus);
    }

    private void registerForgeEvents(IEventBus forgeEventBus) {
        forgeEventBus.addListener(CeiFluids::handleInkEffect);
    }
    
    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CeiAdvancements.register();
            CeiPackets.register();
            CeiFluids.registerLavaReaction();
//            ApotheosisCompat.addPotionMixingRecipes();
//            ApotheosisCompat.banTomeFromEnchanter();
//            QuarkCompat.registerPrintEntry();
        });
    }

    public static ResourceLocation genRL(String name) {
        return ResourceLocation.fromNamespaceAndPath(ID, name);
    }

}
