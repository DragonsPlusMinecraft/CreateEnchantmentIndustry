package plus.dragons.createenchantmentindustry.entry;

import com.jozufozu.flywheel.core.PartialModel;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class ModBlockPartials {
    
    public static final PartialModel
        COPIER_MACHINE_TOP = block("copier_machine/top"),
        COPIER_MACHINE_MIDDLE = block("copier_machine/middle"),
        COPIER_MACHINE_BOTTOM = block("copier_machine/bottom");
    
    private static PartialModel block(String path) {
        return new PartialModel(EnchantmentIndustry.genRL("block/" + path));
    }
    
    public static void register() {
    }
    
}
