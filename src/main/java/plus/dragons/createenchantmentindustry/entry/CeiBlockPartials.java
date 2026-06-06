package plus.dragons.createenchantmentindustry.entry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiBlockPartials {
    public static final PartialModel PRINTER_TOP = block("printer/top"),
            PRINTER_MIDDLE = block("printer/middle"),
            PRINTER_BOTTOM = block("printer/bottom");

    private static PartialModel block(String path) {
        return PartialModel.of(EnchantmentIndustry.genRL("block/" + path));
    }

    public static void register() {}
}
