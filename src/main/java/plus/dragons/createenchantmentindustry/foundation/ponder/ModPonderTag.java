package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.foundation.ponder.PonderTag;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.ModBlocks;

public class ModPonderTag extends PonderTag {

    public static final PonderTag EXPERIENCE = create("experience").item(ModBlocks.DISENCHANTER.get(), true, false).addToIndex();

    public ModPonderTag(ResourceLocation id) {
        super(id);
    }

    private static PonderTag create(String id) {
        return new PonderTag(EnchantmentIndustry.genRL(id));
    }
}
