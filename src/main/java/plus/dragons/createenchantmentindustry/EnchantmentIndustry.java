package plus.dragons.createenchantmentindustry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
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
    public static final SafeRegistrate REGISTRATE = new SafeRegistrate(ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    public static final Lang LANG = new Lang(ID);
    public static final AdvancementFactory ADVANCEMENT_FACTORY = AdvancementFactory.create(NAME, ID,
        CeiAdvancements::register);

    public EnchantmentIndustry(IEventBus modEventBus, ModContainer modContainer) {
        IEventBus forgeEventBus = NeoForge.EVENT_BUS;
        
        CeiConfigs.register(modContainer);

        REGISTRATE.registerEventListeners(modEventBus);
        registerEntries(modEventBus);
        modEventBus.register(this);
        registerForgeEvents(forgeEventBus);
    }

    private void registerEntries(IEventBus modEventBus) {
        CeiCreativeModeTab.register(modEventBus);
        REGISTRATE.setCreativeTab(CeiCreativeModeTab.CREATIVE_TAB);
        CeiBlocks.register();
        CeiBlockEntities.register();
        modEventBus.addListener(CeiBlockEntities::registerCapabilities);
        CeiContainerTypes.register();
        CeiEntityTypes.register();
        CeiFluids.register();
        CeiComponents.register(modEventBus);
        CeiItems.register();
        modEventBus.addListener(EnchantmentIndustry::onRegister);
        CeiRecipeTypes.register(modEventBus);
        CeiTags.register();
        CeiDisplaySources.register();
    }

    private void registerForgeEvents(IEventBus forgeEventBus) {
        forgeEventBus.addListener(CeiFluids::handleInkEffect);
    }

    public static void onRegister(final RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
            CeiAdvancements.register();
        }
    }
    
    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
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
