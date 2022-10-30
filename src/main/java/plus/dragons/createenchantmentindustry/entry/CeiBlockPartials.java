package plus.dragons.createenchantmentindustry.entry;

import com.jozufozu.flywheel.core.PartialModel;
import net.minecraft.client.resources.model.ModelBakery;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;

public class CeiBlockPartials {
    
    public static final PartialModel
        PRINTER_TOP = block("printer/top"),
        PRINTER_MIDDLE = block("printer/middle"),
        PRINTER_BOTTOM = block("printer/bottom");
    
    private static PartialModel block(String path) {
        return new PartialModel(EnchantmentIndustry.genRL("block/" + path));
    }
    
    public static void register() {
        ModelBakery.UNREFERENCED_TEXTURES.add(BlazeEnchanterRenderer.BOOK_MATERIAL);
    }
    
}
