package plus.dragons.createenchantmentindustry.entry;

import com.jozufozu.flywheel.core.PartialModel;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiBlockPartials {
    
    public static final PartialModel
        COPIER_TOP = block("copier/top"),
        COPIER_MIDDLE = block("copier/middle"),
        COPIER_BOTTOM = block("copier/bottom");
    
    private static PartialModel block(String path) {
        return new PartialModel(EnchantmentIndustry.genRL("block/" + path));
    }
    
    public static void register() {
    }
    
}
