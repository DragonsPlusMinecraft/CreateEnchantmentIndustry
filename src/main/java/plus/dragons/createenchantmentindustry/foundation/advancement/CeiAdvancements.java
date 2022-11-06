package plus.dragons.createenchantmentindustry.foundation.advancement;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import net.minecraft.Util;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createdragonlib.advancement.AdvancementHolder;
import plus.dragons.createdragonlib.advancement.critereon.AccumulativeTrigger;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import java.util.Map;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.ADVANCEMENT_FACTORY;

public class CeiAdvancements {
    private static boolean registered = false;
    public static final AdvancementHolder
    START = null,
    // Root
    EXPERIENCED_ENGINEER = ADVANCEMENT_FACTORY.builder("experienced_engineer")
            .title("Experienced Engineer")
            .description("Get some Nuggets of Experience from crushing ores or killing mobs using deployer")
            .icon(AllItems.EXP_NUGGET)
            .externalTrigger("have_experience_nugget", InventoryChangeTrigger.TriggerInstance.hasItems(AllItems.EXP_NUGGET.get()))
            .parent(Create.asResource("display_board_0"))
            .build(),
    // Printer Branch
    BLACK_AS_INK = ADVANCEMENT_FACTORY.builder("black_as_ink")
            .title("Black as Ink!")
            .description("Get a bucket of Ink for your publishing business")
            .icon(CeiFluids.INK.get().getBucket())
            .externalTrigger("have_bucket_of_ink", InventoryChangeTrigger.TriggerInstance.hasItems(CeiFluids.INK.get().getBucket()))
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    COPIABLE_MASTERPIECE = ADVANCEMENT_FACTORY.builder("copiable_masterpiece")
            .title("Copiable Masterpiece")
            .description("Copy a Written Book using Printer")
            .icon(Items.WRITTEN_BOOK)
            .parent(BLACK_AS_INK)
            .build(),
    COPIABLE_MYSTERY = ADVANCEMENT_FACTORY.builder("copiable_mystery")
            .title("Copiable Mystery")
            .description("Copy a Enchanted Book using Printer")
            .icon(Items.ENCHANTED_BOOK)
            .announce(true)
            .parent(COPIABLE_MASTERPIECE)
            .build(),
    RELIC_RESTORATION = ADVANCEMENT_FACTORY.builder("relic_restoration")
            .title("Relic Restoration")
            .description("Make brand new copy from a tattered book")
            .icon(Items.WRITABLE_BOOK)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(COPIABLE_MYSTERY)
            .build(),
    GREAT_PUBLISHER = ADVANCEMENT_FACTORY.builder("great_publisher")
            .title("Great Publisher")
            .description("Copy 1000 books using Printer")
            .externalTrigger("book_copied", new AccumulativeTrigger.TriggerInstance(CeiTriggers.BOOK_PRINTED.getId(), EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(1000)))
            .icon(CeiBlocks.PRINTER)
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(RELIC_RESTORATION)
            .build(),
    EXPERIMENTAL = ADVANCEMENT_FACTORY.builder("experimental")
            .title("Experimental")
            .description("Get some Liquid Experience for your enchanting experiment!")
            .icon(Items.EXPERIENCE_BOTTLE)
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    GONE_WITH_THE_FOIL = ADVANCEMENT_FACTORY.builder("gone_with_the_foil")
            .title("Gone with the Foil")
            .description("Watch an enchanted item be disenchanted by a Disenchanter")
            .icon(CeiBlocks.DISENCHANTER)
            .parent(EXPERIMENTAL)
            .build(),
    SPIRIT_TAKING = ADVANCEMENT_FACTORY.builder("spirit_taking")
            .title("Spirit Taking")
            .description("Get your experience absorbed by a Disenchanter")
            .icon(AllBlocks.MECHANICAL_PUMP)
            .announce(true)
            .parent(GONE_WITH_THE_FOIL)
            .build(),
    A_SHOWER_EXPERIENCE = ADVANCEMENT_FACTORY.builder("a_shower_experience")
            .title("A Shower \"Experience\"")
            .description("Break a Fluid Pipe and bathe in the leaked experience")
            .icon(AllBlocks.FLUID_PIPE)
            .announce(true)
            .frame(FrameType.GOAL)
            .parent(SPIRIT_TAKING)
            .build(),
    EXPERIENCED_RECYCLER = ADVANCEMENT_FACTORY.builder("experienced_recycler")
            .title("Experienced Recycler")
            .description("Recycle 1,000,000 mB of experience from Disenchanter")
            .icon(AllBlocks.COPPER_VALVE_HANDLE)
            .externalTrigger("experience_recycled", new AccumulativeTrigger.TriggerInstance(CeiTriggers.DISENCHANTED.getId(), EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(1000000)))
            .announce(true)
            .frame(FrameType.CHALLENGE)
            .parent(A_SHOWER_EXPERIENCE)
            .build(),
    // Blaze Enchanter Branch
    BLAZES_NEW_JOB = ADVANCEMENT_FACTORY.builder("blazes_new_job")
            .title("Blaze's New Job")
            .description("Give your Blaze Burner a Enchanting Guide and turn it into a Blaze Enchanter")
            .icon(CeiItems.ENCHANTING_GUIDE)
            .parent(EXPERIENCED_ENGINEER)
            .build(),
    FIRST_ORDER = ADVANCEMENT_FACTORY.builder("first_order")
            .title("First Order")
            .description("Add a new enchantment to an unenchanted item using Blaze Enchanter")
            .icon(Items.GOLDEN_HELMET)
            .parent(BLAZES_NEW_JOB)
            .build(),
    ADDITIONAL_ORDER = ADVANCEMENT_FACTORY.builder("additional_order")
            .title("Additional Order")
            .description("Add a new enchantment to an enchanted item using Blaze Enchanter")
            .icon(Util.make(
                    new ItemStack(Items.GOLDEN_HELMET),
                    stack -> EnchantmentHelper.setEnchantments(Map.of(Enchantments.ALL_DAMAGE_PROTECTION, 5), stack)
            ))
            .parent(FIRST_ORDER)
            .build(),
    HYPOTHETICAL_EXTENSION = ADVANCEMENT_FACTORY.builder("hypothetical_extension")
            .title("Hypothetical Extension")
            .description("Add a new enchantment to an item using Blaze Enchanter's hyper-enchanting")
            .icon(Items.DIAMOND_HELMET)
            .parent(ADDITIONAL_ORDER)
            .build(),
    END = null;
    
    public static void register() {
        if (!registered) {
            ADVANCEMENT_FACTORY.register();
        }
        registered = true;
    }
    
}
