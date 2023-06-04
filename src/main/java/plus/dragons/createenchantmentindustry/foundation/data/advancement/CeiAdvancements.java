package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CeiAdvancements implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    public static final List<CeiAdvancement> ENTRIES = new ArrayList<>();
    public static final CeiAdvancement
    START = null,
    // Root
    EXPERIENCED_ENGINEER = builder("experienced_engineer")
        .title("Experienced Engineer")
        .description("Get some Nuggets of Experience from crushing ores or killing mobs using deployer")
        .icon(AllItems.EXP_NUGGET)
        .externalTrigger("have_experience_nugget", InventoryChangeTrigger.TriggerInstance.hasItems(AllItems.EXP_NUGGET.get()))
        .parent(Create.asResource("display_board_0"))
        .build(),
    // Printer Branch
    BLACK_AS_INK = builder("black_as_ink")
        .title("Black as Ink!")
        .description("Get a bucket of Ink for your publishing business")
        .icon(CeiFluids.INK.get().getBucket())
        .externalTrigger("have_bucket_of_ink", InventoryChangeTrigger.TriggerInstance.hasItems(CeiFluids.INK.get().getBucket()))
        .parent(EXPERIENCED_ENGINEER)
        .build(),
    COPIABLE_MASTERPIECE = builder("copiable_masterpiece")
        .title("Copiable Masterpiece")
        .description("Copy a Written Book using Printer")
        .icon(Items.WRITTEN_BOOK)
        .parent(BLACK_AS_INK)
        .build(),
    COPIABLE_MYSTERY = builder("copiable_mystery")
        .title("Copiable Mystery")
        .description("Copy a Enchanted Book using Printer")
        .icon(Items.ENCHANTED_BOOK)
        .announce(true)
        .parent(COPIABLE_MASTERPIECE)
        .build(),
    RELIC_RESTORATION = builder("relic_restoration")
        .title("Relic Restoration")
        .description("Make brand new copy from a tattered book")
        .icon(Items.WRITABLE_BOOK)
        .announce(true)
        .frame(FrameType.GOAL)
        .parent(COPIABLE_MYSTERY)
        .build(),
    EMERGING_BRAND = builder("emerging_brand")
            .title("Emerging Brand")
            .description("Using the printer to name items")
            .icon(Items.NAME_TAG)
            .announce(true)
            .parent(COPIABLE_MASTERPIECE)
            .build(),
    GREAT_PUBLISHER = builder("great_publisher")
        .title("Great Publisher")
        .description("Copy 1000 books using Printer")
        .externalTrigger("book_copied", AccumulativeTrigger.TriggerInstance.ofBook(1000))
        .icon(CeiBlocks.PRINTER)
        .announce(true)
        .frame(FrameType.CHALLENGE)
        .parent(RELIC_RESTORATION)
        .build(),
    // Disenchanter Branch
    EXPERIMENTAL = builder("experimental")
        .title("Experimental")
        .description("Get some Liquid Experience for your enchanting experiment!")
        .icon(Items.EXPERIENCE_BOTTLE)
        .parent(EXPERIENCED_ENGINEER)
        .build(),
    GONE_WITH_THE_FOIL = builder("gone_with_the_foil")
        .title("Gone with the Foil")
        .description("Watch an enchanted item be disenchanted by a Disenchanter")
        .icon(CeiBlocks.DISENCHANTER)
        .parent(EXPERIMENTAL)
        .build(),
    SPIRIT_TAKING = builder("spirit_taking")
        .title("Spirit Taking")
        .description("Get your experience absorbed by a Disenchanter")
        .icon(AllBlocks.MECHANICAL_PUMP)
        .announce(true)
        .parent(GONE_WITH_THE_FOIL)
        .build(),
    A_SHOWER_EXPERIENCE = builder("a_shower_experience")
        .title("A Shower \"Experience\"")
        .description("Break a Fluid Pipe and bathe in the leaked experience")
        .icon(AllBlocks.FLUID_PIPE)
        .announce(true)
        .frame(FrameType.GOAL)
        .parent(SPIRIT_TAKING)
        .build(),
    EXPERIENCED_RECYCLER = builder("experienced_recycler")
        .title("Experienced Recycler")
        .description("Recycle 1,000,000 mB of experience from Disenchanter")
        .icon(AllBlocks.COPPER_VALVE_HANDLE)
        .externalTrigger("experience_recycled", AccumulativeTrigger.TriggerInstance.ofExperienceDisenchanted(1000000))
        .announce(true)
        .frame(FrameType.CHALLENGE)
        .parent(A_SHOWER_EXPERIENCE)
        .build(),
    // Blaze Enchanter Branch
    BLAZES_NEW_JOB = builder("blazes_new_job")
        .title("Blaze's New Job")
        .description("Give your Blaze Burner a Enchanting Guide and turn it into a Blaze Enchanter")
        .icon(CeiItems.ENCHANTING_GUIDE)
        .parent(EXPERIENCED_ENGINEER)
        .build(),
    FIRST_ORDER = builder("first_order")
        .title("First Order")
        .description("Add a new enchantment to an unenchanted item using Blaze Enchanter")
        .icon(Items.GOLDEN_HELMET)
        .parent(BLAZES_NEW_JOB)
        .build(),
    ADDITIONAL_ORDER = builder("additional_order")
        .title("Additional Order")
        .description("Add a new enchantment to an enchanted item using Blaze Enchanter")
        .icon(Util.make(
            new ItemStack(Items.GOLDEN_HELMET),
            stack -> EnchantmentHelper.setEnchantments(Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5), stack)
        ))
        .parent(FIRST_ORDER)
        .build(),
    HYPOTHETICAL_EXTENSION = builder("hypothetical_extension")
        .title("Hypothetical Extension")
        .description("Add a new enchantment to an item using Blaze Enchanter's hyper-enchanting")
        .icon(Items.DIAMOND_HELMET)
        .parent(ADDITIONAL_ORDER)
        .build(),
    END = null;

    //Shortcut for builder constructor
    public static CeiAdvancement.Builder builder(String id) {
        return new CeiAdvancement.Builder(id);
    }

    //Datagen
    private final DataGenerator generator;

    public CeiAdvancements(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(HashCache cache) {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = advancement -> {
            if (!set.add(advancement.getId()))
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            Path advancementPath = path.resolve("data/"
                + advancement.getId().getNamespace() + "/advancements/"
                + advancement.getId().getPath() + ".json"
            );
            try {
                DataProvider.save(GSON, cache, advancement.deconstruct().serializeToJson(), advancementPath);
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't save advancement {}", advancementPath, ioexception);
            }
        };
        for (var advancement : ENTRIES) {
            advancement.save(consumer);
        }
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Advancements";
    }

    public static JsonObject provideLangEntries() {
        JsonObject object = new JsonObject();
        for (var advancement : ENTRIES) {
            advancement.appendToLang(object);
        }
        return object;
    }

    public static void register() {}

}
