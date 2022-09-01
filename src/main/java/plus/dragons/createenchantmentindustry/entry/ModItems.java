package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.EnchantingGuideItem;

public class ModItems {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            // TODO Consider should we make our own tab?
            .creativeModeTab(() -> Create.BASE_CREATIVE_TAB);

    static{
        REGISTRATE.startSection(AllSections.KINETICS);
    }

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE_FOR_BLAZE = REGISTRATE.item("enchanting_guide_for_blaze", EnchantingGuideItem::new).register();

    public static void register(){}
}
