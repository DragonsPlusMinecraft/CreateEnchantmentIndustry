package plus.dragons.createenchantmentindustry.entry;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.RegistryEvent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.api.event.FillCreateItemGroupEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceRotorItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottleItem;

public class CeiItems {

    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            .creativeModeTab(() -> Create.BASE_CREATIVE_TAB).startSection(AllSections.KINETICS);

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE = REGISTRATE.item("enchanting_guide", EnchantingGuideItem::new)
            .properties(prop -> prop.stacksTo(1))
            .register();
    
    static {
        REGISTRATE.startSection(AllSections.MATERIALS);
    }

    public static final ItemEntry<HyperExperienceBottleItem> HYPER_EXP_BOTTLE = REGISTRATE.item("hyper_experience_bottle", HyperExperienceBottleItem::new)
            .properties(prop -> prop.rarity(Rarity.RARE))
            .lang("Bottle O' Hyper Enchanting")
            .register();
    
    public static final ItemEntry<ExperienceRotorItem> EXPERIENCE_ROTOR = REGISTRATE.item("experience_rotor", ExperienceRotorItem::new)
            .register();

    public static void fillCreateItemGroup(FillCreateItemGroupEvent event) {
        if (event.getItemGroup() == Create.BASE_CREATIVE_TAB) {
            event.addInsertion(AllBlocks.ITEM_DRAIN.get(), CeiBlocks.DISENCHANTER.asStack());
            event.addInsertion(AllBlocks.SPOUT.get(), CeiBlocks.PRINTER.asStack());
            event.addInsertion(AllBlocks.BLAZE_BURNER.get(), ENCHANTING_GUIDE.asStack());
            event.addInsertion(AllItems.ELECTRON_TUBE.get(), EXPERIENCE_ROTOR.asStack());
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), CeiFluids.INK.get().getBucket().getDefaultInstance());
            event.addInsertion(AllFluids.CHOCOLATE.get().getBucket(), HYPER_EXP_BOTTLE.asStack());
        }
    }
    
    public static void remap(RegistryEvent.MissingMappings<Item> event) {
        var mappings = event.getMappings(EnchantmentIndustry.ID);
        var remaps = ImmutableMap.<ResourceLocation, ItemProviderEntry<?>>builder()
            .put(EnchantmentIndustry.genRL("copier"), CeiBlocks.PRINTER)
            .build();
        for (var mapping : mappings) {
            var remap = remaps.get(mapping.key);
            if (remap != null) {
                mapping.remap(remap.get().asItem());
                EnchantmentIndustry.LOGGER.warn("Remapping item [{}] to [{}]...", mapping.key, remap.getId());
            }
        }
    }
    
    public static void register() {}
    
}