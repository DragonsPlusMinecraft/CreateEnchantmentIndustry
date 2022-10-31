package plus.dragons.createenchantmentindustry.foundation.config;

import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.config.ui.ConfigAnnotations;
import net.minecraftforge.common.ForgeConfigSpec;

public class CeiServerConfig extends ConfigBase {

    public final ConfigInt disenchanterTankCapacity = i(1500, 0,
            "disenchanterTankCapacity",
            Comments.disenchanterTankCapacity,
            ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt copierTankCapacity = i(3000, 0,
            "copierTankCapacity",
            Comments.copierTankCapacity,
            ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt blazeEnchanterTankCapacity = i(3000, 0,
            "blazeEnchanterTankCapacity",
            Comments.blazeEnchanterTankCapacity,
            ConfigAnnotations.RequiresRestart.SERVER.asComment());
    public final ConfigInt maxHyperEnchantingLevelExtension = i(2, 0,
            "maxHyperEnchantingLevelExtension",
            Comments.maxHyperEnchantingLevelExtension);

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
                "The Maximum Extended Levels beyond Enchantment's Max Level that can be reached through Hyper-Enchanting.";

    }

}
