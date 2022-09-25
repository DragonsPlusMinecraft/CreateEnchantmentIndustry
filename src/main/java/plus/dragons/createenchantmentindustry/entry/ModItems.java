package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.api.event.FillCreateItemGroupEvent;

public class ModItems {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            .creativeModeTab(() -> Create.BASE_CREATIVE_TAB).startSection(AllSections.KINETICS);

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE = REGISTRATE.item("enchanting_guide", EnchantingGuideItem::new)
            .properties(prop -> prop.stacksTo(1))
            .register();

    public static void fillCreateItemGroup(FillCreateItemGroupEvent event) {
        if (event.getItemGroup() == Create.BASE_CREATIVE_TAB) {
            event.addInsertion(AllBlocks.ITEM_DRAIN.get(), ModBlocks.DISENCHANTER.asStack());
            event.addInsertion(AllBlocks.SPOUT.get(), ModBlocks.COPIER.asStack());
            event.addInsertion(AllBlocks.BLAZE_BURNER.get(), ENCHANTING_GUIDE.asStack());
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), ModFluids.INK.get().getBucket().getDefaultInstance());
        }
    }
    
    public static void register() {
    }
}
