package plus.dragons.createenchantmentindustry.foundation.config;

import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.config.ui.ConfigAnnotations;
import net.minecraftforge.common.ForgeConfigSpec;

public class CeiServerConfig extends ConfigBase {
    
    public final ConfigInt disenchanterTankCapacity = i(1000, 0,
        "disenchanterTankCapacity",
        Comments.disenchanterTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt copierTankCapacity = i(4000, 0,
        "copierTankCapacity",
        Comments.copierTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt blazeEnchanterTankCapacity = i(2000, 0,
        "blazeEnchanterTankCapacity",
        Comments.blazeEnchanterTankCapacity,
        ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt maxHyperEnchantingLevelExtension = i(2, 0,
        "maxHyperEnchantingLevelExtension",
        Comments.maxHyperEnchantingLevelExtension);
    public final ConfigFloat deployerXpDropChance = f(1, 0, 1,
        "deployerXpDropChance",
        Comments.deployerXpDropChance);

    public final ConfigBool enableHyperEnchant = b(true, "enableHyperEnchant");
    
    @Override
    protected void registerAll(ForgeConfigSpec.Builder builder) {
        super.registerAll(builder);
    }

    @Override
    public String getName() {
        return "server";
    }
    
    private static class Comments {
    
        static String disenchanterTankCapacity =
            "The Tank Capacity of the Disenchanter";
        static String copierTankCapacity =
            "The Tank Capacity of the Copier";
        static String blazeEnchanterTankCapacity =
            "The Tank Capacity of the Blaze Enchanter";
        static String maxHyperEnchantingLevelExtension =
            "The Maximum Extended Levels beyond Enchantment's Max Level that can be reached through Hyper-Enchanting";
        static String deployerXpDropChance =
            "The Chance of whether Deployer-killed entities will drop Nugget of Experience";
        
    }

}
