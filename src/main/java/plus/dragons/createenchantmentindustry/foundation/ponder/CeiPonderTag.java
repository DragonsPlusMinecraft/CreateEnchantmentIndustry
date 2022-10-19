package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.foundation.ponder.PonderTag;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

public class CeiPonderTag extends PonderTag {

    public static final PonderTag EXPERIENCE = create("experience").item(CeiBlocks.DISENCHANTER.get(), true, false).addToIndex();

    public CeiPonderTag(ResourceLocation id) {
        super(id);
    }

    private static PonderTag create(String id) {
        return new PonderTag(EnchantmentIndustry.genRL(id));
    }
}
