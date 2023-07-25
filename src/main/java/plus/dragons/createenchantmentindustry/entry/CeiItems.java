package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.*;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
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

    public static void fillCreateItemGroup(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == AllCreativeModeTabs.getBaseTab()) {
            MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> entries = event.getEntries();
            entries.putAfter(AllBlocks.ITEM_DRAIN.asStack(), CeiBlocks.DISENCHANTER.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.putAfter(AllBlocks.SPOUT.asStack(), CeiBlocks.PRINTER.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.putAfter(AllBlocks.BLAZE_BURNER.asStack(), ENCHANTING_GUIDE.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.putAfter(AllItems.ELECTRON_TUBE.asStack(), EXPERIENCE_ROTOR.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.putAfter(AllFluids.CHOCOLATE.get().getBucket().getDefaultInstance(), CeiFluids.INK.get().getBucket().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            entries.putAfter(AllFluids.CHOCOLATE.get().getBucket().getDefaultInstance(), HYPER_EXP_BOTTLE.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    public static void register() {}
    
}