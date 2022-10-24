package plus.dragons.createenchantmentindustry.entry;

import com.jozufozu.flywheel.core.PartialModel;
import net.minecraft.client.resources.model.ModelBakery;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;

public class CeiBlockPartials {
    
    public static final PartialModel
        COPIER_TOP = block("copier/top"),
        COPIER_MIDDLE = block("copier/middle"),
        COPIER_BOTTOM = block("copier/bottom");
    
    private static PartialModel block(String path) {
        return new PartialModel(EnchantmentIndustry.genRL("block/" + path));
    }
    
    public static void register() {
        ModelBakery.UNREFERENCED_TEXTURES.add(BlazeEnchanterRenderer.BOOK_MATERIAL);
    }
    
}
