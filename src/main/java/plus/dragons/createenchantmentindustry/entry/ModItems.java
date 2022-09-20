package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchantments.EnchantingGuideItem;

public class ModItems {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            .creativeModeTab(() -> EnchantmentIndustry.CREATIVE_TAB);

    static {
        REGISTRATE.startSection(AllSections.KINETICS);
    }

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE_FOR_BLAZE = REGISTRATE.item("enchanting_guide_for_blaze", EnchantingGuideItem::new)
            .properties(prop -> prop.stacksTo(1))
            .lang("Enchanting Guide for Blaze")
            .register();

    public static void register() {
    }
}
