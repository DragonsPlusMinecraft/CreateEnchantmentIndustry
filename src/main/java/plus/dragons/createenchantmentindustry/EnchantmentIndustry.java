package plus.dragons.createenchantmentindustry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.AdvancementFactory;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.init.SafeRegistrate;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.lang.Lang;
import plus.dragons.createenchantmentindustry.compat.apotheosis.ApotheosisCompat;
import plus.dragons.createenchantmentindustry.compat.quark.QuarkCompat;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceOpenPipeEffectHandler;
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

    public EnchantmentIndustry() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        
        CeiConfigs.register(ModLoadingContext.get());
        
        registerEntries(modEventBus);
        modEventBus.register(this);
        registerForgeEvents(forgeEventBus);
        
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> EnchantmentIndustryClient::new);
    }

    private void registerEntries(IEventBus modEventBus) {
        CeiBlocks.register();
        CeiBlockEntities.register();
        CeiContainerTypes.register();
        CeiEntityTypes.register();
        CeiFluids.register();
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
            CeiPackets.registerPackets();
            CeiFluids.registerLavaReaction();
            ExperienceOpenPipeEffectHandler.register();
            ApotheosisCompat.addPotionMixingRecipes();
            ApotheosisCompat.banTomeFromEnchanter();
            QuarkCompat.registerPrintEntry();
        });
    }

    public static ResourceLocation genRL(String name) {
        return new ResourceLocation(ID, name);
    }

}
