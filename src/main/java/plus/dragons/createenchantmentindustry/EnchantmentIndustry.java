package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
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
import plus.dragons.createdragonlib.lang.AutoLang;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.OpenEndedPipeEffects;
import plus.dragons.createenchantmentindustry.entry.*;
import plus.dragons.createenchantmentindustry.foundation.config.ModConfigs;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;

@Mod(EnchantmentIndustry.MOD_ID)
public class EnchantmentIndustry {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "create_enchantment_industry";
    private static final NonNullSupplier<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(MOD_ID);

    public EnchantmentIndustry() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        ModConfigs.register(ModLoadingContext.get());
        initAllEntries();
        CeiRecipeTypes.register(modEventBus);

        addForgeEventListeners(forgeEventBus);
        modEventBus.addListener(EnchantmentIndustry::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EnchantmentIndustryClient.onClient(modEventBus, forgeEventBus));

        var autoLang = AutoLang.create("Create Enchantment Industry", MOD_ID)
                .enableForAdvancement(CeiAdvancements::register)
                .enableForPonders(() -> {
                    CeiPonderIndex.register();
                    CeiPonderIndex.registerTags();
                })
                .mergeCreateStyleTooltipLang()
                .mergeCreateStyleInterfaceLang();

        modEventBus.addListener(EventPriority.LOWEST, autoLang::registerDatagen);
        modEventBus.addListener(EventPriority.LOWEST, CeiAdvancements::registerDataGen);
    }

    private void initAllEntries() {
        CeiItems.register();
        CeiBlocks.register();
        CeiBlockEntities.register();
        CeiEntityTypes.register();
        CeiFluids.register();
        CeiContainerTypes.register();
        CeiTags.register();
    }

    private void addForgeEventListeners(IEventBus forgeEventBus) {
        forgeEventBus.addListener(CeiItems::fillCreateItemGroup);
        forgeEventBus.addListener(CeiFluids::handleInkEffect);
        forgeEventBus.addListener(CeiFluids::handleInkLavaReaction);
    }

    public static void init(final FMLCommonSetupEvent event) {
        CeiAdvancements.register();
        event.enqueueWork(() -> {
            CeiPackets.registerPackets();
            OpenEndedPipeEffects.register();
        });
    }

    public static ResourceLocation genRL(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE.get();
    }
}
