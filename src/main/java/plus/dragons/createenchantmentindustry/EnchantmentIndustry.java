package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.OpenEndedPipeEffects;
import plus.dragons.createenchantmentindustry.entry.*;

@Mod("create_enchantment_industry")
public class EnchantmentIndustry {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "create_enchantment_industry";
    private static final NonNullSupplier<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(MOD_ID);

    public EnchantmentIndustry() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                .getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        initAllEntries();
        OpenEndedPipeEffects.register();

        modEventBus.addListener(EnchantmentIndustry::init);
    }

    private void initAllEntries() {
        ModItems.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModFluids.register();
        ModContainerTypes.register();
    }

    public static void init(final FMLCommonSetupEvent event) {
        ModPackets.registerPackets();
    }

    public static ResourceLocation genRL(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE.get();
    }
}
