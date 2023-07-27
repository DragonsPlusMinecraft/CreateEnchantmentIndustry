package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Rarity;
import plus.dragons.createdragonlib.init.FillCreateItemGroupEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceRotorItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottleItem;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiItems {

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE = REGISTRATE.item("enchanting_guide", EnchantingGuideItem::new)
            .properties(prop -> prop.stacksTo(1))
            .register();

    public static final ItemEntry<HyperExperienceBottleItem> HYPER_EXP_BOTTLE = REGISTRATE.item("hyper_experience_bottle", HyperExperienceBottleItem::new)
            .properties(prop -> prop.rarity(Rarity.RARE))
            .lang("Bottle O' Hyper Enchanting")
            .tag(CeiTags.ItemTag.UPRIGHT_ON_BELT.tag)
            .register();

    public static final ItemEntry<ExperienceRotorItem> EXPERIENCE_ROTOR = REGISTRATE.item("experience_rotor", ExperienceRotorItem::new)
            .register();

    public static void fillCreateItemGroup(FillCreateItemGroupEvent event) {
        if (event.isBase()) {
            event.addInsertion(AllBlocks.ITEM_DRAIN, CeiBlocks.DISENCHANTER);
            event.addInsertion(AllBlocks.SPOUT, CeiBlocks.PRINTER);
            event.addInsertion(AllBlocks.BLAZE_BURNER, ENCHANTING_GUIDE);
            event.addInsertion(AllItems.ELECTRON_TUBE, EXPERIENCE_ROTOR);
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), CeiFluids.INK.get().getBucket());
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), HYPER_EXP_BOTTLE);
        }
    }

    public static void register() {}
    
}