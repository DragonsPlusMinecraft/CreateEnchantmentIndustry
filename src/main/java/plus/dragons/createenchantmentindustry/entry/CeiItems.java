package plus.dragons.createenchantmentindustry.entry;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.*;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import plus.dragons.createdragonlib.init.FillCreateItemGroupEvent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceRotorItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottleItem;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiItems {
    
    static {
        Create.REGISTRATE.creativeModeTab(() -> AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }

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
        if (event.getItemGroup() == AllCreativeModeTabs.BASE_CREATIVE_TAB) {
            event.addInsertion(AllBlocks.ITEM_DRAIN.get(), CeiBlocks.DISENCHANTER.asStack());
            event.addInsertion(AllBlocks.SPOUT.get(), CeiBlocks.PRINTER.asStack());
            event.addInsertion(AllBlocks.BLAZE_BURNER.get(), ENCHANTING_GUIDE.asStack());
            event.addInsertion(AllItems.ELECTRON_TUBE.get(), EXPERIENCE_ROTOR.asStack());
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), CeiFluids.INK.get().getBucket().getDefaultInstance());
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), HYPER_EXP_BOTTLE.asStack());
        }
    }
    
    public static void remap(MissingMappingsEvent event) {
        var mappings = event.getMappings(ForgeRegistries.Keys.ITEMS, EnchantmentIndustry.ID);
        var remaps = ImmutableMap.<ResourceLocation, ItemProviderEntry<?>>builder()
            .put(EnchantmentIndustry.genRL("copier"), CeiBlocks.PRINTER)
            .build();
        for (var mapping : mappings) {
            var key = mapping.getKey();
            var remap = remaps.get(key);
            if (remap != null) {
                mapping.remap(remap.get().asItem());
                EnchantmentIndustry.LOGGER.warn("Remapping item [{}] to [{}]...", key, remap.getId());
            }
        }
    }

    public static void register() {}
    
}