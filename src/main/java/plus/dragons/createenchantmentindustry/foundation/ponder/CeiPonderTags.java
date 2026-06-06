package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class CeiPonderTags {
    public static final ResourceLocation EXPERIENCE = EnchantmentIndustry.genRL("experience");

    public CeiPonderTags() {}

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(EXPERIENCE).addToIndex().item(CeiBlocks.DISENCHANTER.get(), true, false).title("Experience Related").description("Items and Components related to experience").register();

        PonderTagRegistrationHelper<RegistryEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.addToTag(EXPERIENCE)
                .add(CeiBlocks.DISENCHANTER)
                .add(CeiBlocks.PRINTER)
                .add(CeiItems.ENCHANTING_GUIDE)
                .add(AllItems.EXP_NUGGET)
                .add(CeiItems.HYPER_EXP_BOTTLE)
                .add(AllBlocks.ITEM_DRAIN)
                .add(AllBlocks.SPOUT);
    }
}
