package plus.dragons.createenchantmentindustry;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import plus.dragons.createenchantmentindustry.registry.ModRegistries;

@Mod("create_enchantment_industry")
public class EnchantmentIndustry
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "create_enchantment_industry";

    public EnchantmentIndustry()
    {
        ModRegistries.init();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }

    public static ResourceLocation genRL(String name){
        return new ResourceLocation(MOD_ID,name);
    }
}
