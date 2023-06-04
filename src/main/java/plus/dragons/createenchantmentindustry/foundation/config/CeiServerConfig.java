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

    public final ConfigFloat deployerXpDropScale= f(0.34F, 0.1F, 100,
            "deployerXpDropScale",
            Comments.deployerXpDropScale);
    public final ConfigBool enableHyperEnchant = b(true, "enableHyperEnchant");

    public final ConfigInt copyWrittenBookCostPerPage = i(5, 1, 100,
            "copyWrittenBookCostPerPage",
            Comments.copyWrittenBookCostPerPage);
    public final ConfigInt copyNameTagCost = i(7, 1, 1000,
            "copyNameTagCost",
            Comments.copyNameTagCost);
    public final ConfigInt copyTrainScheduleCost = i(10, 1, 1000,
            "copyTrainScheduleCost",
            Comments.copyTrainScheduleCost);
    public final ConfigFloat crushingWheelDropExpRate = f(0.3f, 0, 1,
            "crushingWheelDropExpRate",
            Comments.crushingWheelDropExpRate);
    public final ConfigBool copyingWrittenBookAlwaysGetOriginalVersion = b(true,
            "copyingWrittenBookAlwaysGetOriginalVersion",
            Comments.copyingWrittenBookAlwaysGetOriginalVersion);
    
    @Override
    public void registerAll(ForgeConfigSpec.Builder builder) {
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
            "The Chance of whether Deployer-killed entities will drop Experience Nugget";
        static String deployerXpDropScale =
                "The Scale of Experience Nugget dropped by Deployer-killed entities";
        static String copyWrittenBookCostPerPage = "The amount of ink needed to be consumed by Copying one page of Written Book";
        static String copyNameTagCost = "The amount of liquid experience needed to be consumed by Copying Name Tag";
        static String copyTrainScheduleCost = "The amount of ink needed to be consumed by Copying Train Schedule";
        static String crushingWheelDropExpRate = "The probability of dropping Experience Nugget after killing a creature on the Crushing Wheel";
        static String copyingWrittenBookAlwaysGetOriginalVersion =
                "Whether or not copying a written book always get original version. Setting it to false let you always get copy version of the book.";
        
    }
    
}
