package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.entry.ModBlockEntities;
import plus.dragons.createenchantmentindustry.entry.ModBlocks;
import plus.dragons.createenchantmentindustry.entry.ModFluids;

@Mod("create_enchantment_industry")
public class EnchantmentIndustry
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "create_enchantment_industry";
    private static final NonNullSupplier<CreateRegistrate> REGISTRATE = CreateRegistrate.lazy(MOD_ID);

    public EnchantmentIndustry()
    {
        initAllEntries();
    }

    private void initAllEntries(){
        ModBlocks.register();
        ModBlockEntities.register();
        ModFluids.register();
    }

    public static ResourceLocation genRL(String name){
        return new ResourceLocation(MOD_ID,name);
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE.get();
    }
}
