package plus.dragons.createenchantmentindustry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createdragonlib.advancement.AdvancementFactory;
import plus.dragons.createdragonlib.init.SafeRegistrate;
import plus.dragons.createdragonlib.lang.Lang;
import plus.dragons.createdragonlib.lang.LangFactory;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.OpenEndedPipeEffects;
import plus.dragons.createenchantmentindustry.entry.*;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.config.ModConfigs;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;

@Mod(EnchantmentIndustry.ID)
public class EnchantmentIndustry {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String NAME = "Create: Enchantment Industry";
    public static final String ID = "create_enchantment_industry";
    public static final SafeRegistrate REGISTRATE = new SafeRegistrate(ID);
    public static final Lang LANG = new Lang(ID);
    public static final AdvancementFactory ADVANCEMENT_FACTORY = AdvancementFactory.create(NAME, ID);
    public static final LangFactory LANG_FACTORY = LangFactory.create(NAME, ID)
        .advancements(CeiAdvancements::register)
        .ponders(() -> {
            CeiPonderIndex.register();
            CeiPonderIndex.registerTags();
        })
        .tooltips()
        .ui();

    public EnchantmentIndustry() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        
        ModConfigs.register(ModLoadingContext.get());
        
        registerEntries(modEventBus);
        registerForgeEvents(forgeEventBus);
        
        modEventBus.addListener(EnchantmentIndustry::setup);
        modEventBus.addListener(ADVANCEMENT_FACTORY::datagen);
        modEventBus.addListener(EventPriority.LOWEST, LANG_FACTORY::datagen);
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EnchantmentIndustryClient.onClient(modEventBus, forgeEventBus));
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
        REGISTRATE.registerEventListeners(modEventBus);
    }

    private void registerForgeEvents(IEventBus forgeEventBus) {
        forgeEventBus.addListener(CeiBlockEntities::remap);
        forgeEventBus.addListener(CeiFluids::handleInkEffect);
        forgeEventBus.addListener(CeiItems::fillCreateItemGroup);
    }

    public static void setup(final FMLCommonSetupEvent event) {
        CeiAdvancements.register();
        event.enqueueWork(() -> {
            CeiPackets.registerPackets();
            CeiFluids.registerLavaReaction();
            OpenEndedPipeEffects.register();
        });
    }

    public static ResourceLocation genRL(String name) {
        return new ResourceLocation(ID, name);
    }

}
