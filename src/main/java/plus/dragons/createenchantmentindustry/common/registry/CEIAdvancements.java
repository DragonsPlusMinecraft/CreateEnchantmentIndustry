/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.registry;

import com.google.common.collect.Sets;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.advancement.AllTriggers;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Items;
import plus.dragons.createdragonsplus.common.advancements.CDPAdvancement;
import plus.dragons.createdragonsplus.common.advancements.criterion.BuiltinTrigger;
import plus.dragons.createdragonsplus.common.registry.CDPItems;
import plus.dragons.createdragonsplus.util.CodeReference;
import plus.dragons.createenchantmentindustry.util.CEIAdvancement;

public class CEIAdvancements implements DataProvider {
    public static final List<CDPAdvancement> ENTRIES = new ArrayList<>();
    public static final CDPAdvancement START = null,

            ROOT = create("root", b -> b.icon(CEIItems.EXPERIENCE_BUCKET)
                    .title("Welcome to Create: Enchantment Industry")
                    .description("Road to master enchanting begins")
                    .awardedForFree()
                    .special(CDPAdvancement.TaskType.SILENT)),

            EXPERIENCED_ENGINEER = create("experienced_engineer", b -> b.icon(AllItems.EXP_NUGGET)
                    .title("Experienced Engineer")
                    .description("Obtain some Nuggets of Experience")
                    .whenIconCollected()
                    .after(ROOT)),

            SPIRIT_TAKING = create("spirit_taking", b -> b.icon(AllItems.EXP_NUGGET)
                    .title("Spirit-Taking")
                    .description("Store your experience using an Experience Hatch")
                    .after(EXPERIENCED_ENGINEER)),

            SPIRITUAL_RETURN = create("spiritual_return", b -> b.icon(AllItems.EXP_NUGGET)
                    .title("Spiritual Return")
                    .description("Retrieve some experience using an Experience Hatch")
                    .after(SPIRIT_TAKING)),

            A_SHOWER_EXPERIENCE = create("a_shower_experience", b -> b.icon(AllItems.EXP_NUGGET)
                    .title("A Shower \"Experience\"")
                    .description("Break a Fluid Pipe and bathe in the leaked experience")
                    .special(CDPAdvancement.TaskType.SECRET)
                    .after(SPIRITUAL_RETURN)),

            LUMEN_NEXUS = create("lumen_nexus", b -> b.icon(CEIBlocks.EXPERIENCE_LANTERN)
                    .title("Lumen Nexus")
                    .whenIconCollected()
                    .description("Obtain an Experience Lantern")
                    .after(SPIRITUAL_RETURN)),

            // Mechanical Grindstone
            GONE_WITH_THE_FOIL = create("gone_with_the_foil", b -> b.icon(CEIBlocks.MECHANICAL_GRINDSTONE)
                    .title("Gone with the Foil")
                    .description("Watch an enchanted item be disenchanted by a Mechanical Grindstone")
                    .after(EXPERIENCED_ENGINEER)),

            GRIND_TO_POLISH = create("grind_to_polish", b -> b.icon(AllItems.SAND_PAPER)
                    .title("Grind to Polish")
                    .description("Sandpaper? I've got a better one")
                    .special(CDPAdvancement.TaskType.NOISY)
                    .after(GONE_WITH_THE_FOIL)),

            EXPERIENCED_RECYCLER = create("experienced_recycler", b -> b.icon(CEIBlocks.GRINDSTONE_DRAIN)
                    .title("Experienced Recycler")
                    .description("Obtain 1,000,000 mB of Liquid Experience from Mechanical Grindstones")
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .whenStatReach(Stats.CUSTOM.get(CEIStats.GRINDSTONE_EXPERIENCE.get()), MinMaxBounds.Ints.atLeast(1000000))
                    .after(GONE_WITH_THE_FOIL)),

            // Blaze Enchanter
            BLAZE_ENCHANTERY = create("blaze_enchantery", b -> b.icon(CEIBlocks.BLAZE_ENCHANTER)
                    .title("Blaze Enchantry")
                    .description("Blazes can do more than boil water. Obtain a Blaze Enchanter")
                    .whenIconCollected()
                    .after(EXPERIENCED_ENGINEER)),

            BLAZING_ENCHANTMENT = create("blazing_enchantment", b -> b.icon(Items.GOLDEN_HELMET)
                    .title("Blazing Enchantment")
                    .description("Enchant an unenchanted item using a Blaze Enchanter")
                    .after(BLAZE_ENCHANTERY)),

            SIGIL_FORGING = create("sigil_forging", b -> b.icon(CEIItems.ENCHANTING_TEMPLATE)
                    .title("Sigil Forging")
                    .description("Add a new enchantment to an Enchanting Template using a Blaze Enchanter")
                    .after(BLAZING_ENCHANTMENT)),

            THOUSAND_RUNES = create("thousand_runes", b -> b.icon(CEIBlocks.BLAZE_ENCHANTER)
                    .title("Thousand Runes")
                    .description("Use a Blaze Enchanter 1,000 times")
                    .whenStatReach(Stats.CUSTOM.get(CEIStats.ENCHANT.get()), MinMaxBounds.Ints.atLeast(1000))
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(SIGIL_FORGING)),

            // Blaze Forger
            BORN_TALENT_OF_FIRE = create("born_talent_of_fire", b -> b.icon(CEIBlocks.BLAZE_FORGER)
                    .title("Born Talent of Fire")
                    .description("Blazes were born for this. Obtain a Blaze Forger")
                    .whenIconCollected()
                    .after(EXPERIENCED_ENGINEER)),

            BLAZING_FUSION = create("blazing_fusion", b -> b.icon(Items.GOLDEN_SWORD)
                    .title("Blazing Fusion")
                    .description("Merge two of the same item using a Blaze Forger")
                    .after(BORN_TALENT_OF_FIRE)),

            SIGIL_CASTING = create("sigil_casting", b -> b.icon(CEIItems.ENCHANTING_TEMPLATE)
                    .title("Sigil Casting")
                    .description("Apply an Enchanting Template using a Blaze Forger")
                    .after(BLAZING_FUSION)),

            MAGIC_UNBINDING = create("magic_unbinding", b -> b.icon(CEIItems.ENCHANTING_TEMPLATE)
                    .title("Magic Unbinding")
                    .description("Strip an item's enchantment off using a Blaze Forger")
                    .after(SIGIL_CASTING)),

            BLAZING_CENTURION = create("blazing_centurion", b -> b.icon(CEIBlocks.BLAZE_FORGER)
                    .title("Blazing Centurion")
                    .description("Use a Blaze Forger 1,000 times")
                    .whenStatReach(Stats.CUSTOM.get(CEIStats.FORGE.get()), MinMaxBounds.Ints.atLeast(1000))
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(MAGIC_UNBINDING)),

            // Super Enchant
            LIGHTNING_CATALYSIS = create("lightning_catalysis", b -> b.icon(CEIBlocks.BLAZE_FORGER)
                    .title("Lightning Catalysis")
                    .description("Obtain Super Experience")
                    .whenItemCollected(CEIBlocks.SUPER_EXPERIENCE_BLOCK)
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(EXPERIENCED_ENGINEER)),

            PROBABILITY_SPIKE = create("probability_spike", b -> b.icon(Items.DIAMOND)
                    .title("Probability Spike")
                    .description("How did all these treasures get here?")
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(LIGHTNING_CATALYSIS)),

            TRANSCENDENT_OVERCLOCK = create("transcendent_overclock", b -> b.icon(Items.EMERALD)
                    .title("Transcendent Overclock")
                    .description("How is this possible? Enchantment level caps don't exist?")
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(PROBABILITY_SPIKE)),

            PARADOX_FUSION = create("paradox_fusion", b -> b.icon(Items.REDSTONE)
                    .title("Paradox Fusion")
                    .description("How is this possible? They shouldn't appear at the same time!")
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(TRANSCENDENT_OVERCLOCK)),

            OSHA_VIOLATION = create("osha_violation", b -> b.icon(Items.BARRIER)
                    .title("OSHA Violation")
                    .description("You should NOT have let this happen!!!")
                    .special(CDPAdvancement.TaskType.SECRET)
                    .after(LIGHTNING_CATALYSIS)),

            OMNI_ENCHANTER = create("omni_enchanter", b -> b.icon(Items.NETHER_STAR)
                    .title("Omni-Enchanter")
                    .description("Unbelievable! You've Super Enchanted 1,000 times! Now the number speaks for itself")
                    .whenStatReach(Stats.CUSTOM.get(CEIStats.SUPER_ENCHANT.get()), MinMaxBounds.Ints.atLeast(100))
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(PARADOX_FUSION)),

            // Printer
            COPIABLE_MASTERPIECE = create("copiable_masterpiece", b -> b.icon(Items.WRITTEN_BOOK)
                    .title("Copiable Masterpiece")
                    .description("Copy a Written Book using a Printer")
                    .after(EXPERIENCED_ENGINEER)),

            COPIABLE_MYSTERY = create("copiable_mystery", b -> b.icon(Items.ENCHANTED_BOOK)
                    .title("Copiable Mystery")
                    .description("Copy an Enchanted Book using a Printer")
                    .after(COPIABLE_MASTERPIECE)),

            BRAND_REGISTRY = create("brand_registry", b -> b.icon(Items.NAME_TAG)
                    .title("Brand Registry")
                    .description("Use a Printer to rename an item")
                    .after(COPIABLE_MYSTERY)),

            SUPPLY_CHAIN_REFACTOR = create("supply_chain_refactor", b -> b.icon(PackageStyles.getDefaultBox())
                    .title("Supply Chain Refactor")
                    .description("Use a Printer to change a package's address")
                    .after(BRAND_REGISTRY)),

            ASSEMBLY_AESTHETICS = create("assembly_aesthetics", b -> b.icon(CDPItems.RARE_MARBLE_GATE_PACKAGE)
                    .title("Assembly Aesthetics")
                    .description("Use a Printer to change a package's pattern")
                    .after(SUPPLY_CHAIN_REFACTOR)),

            GREAT_PUBLISHER = create("great_publisher", b -> b.icon(CEIBlocks.PRINTER)
                    .title("Great Publisher")
                    .description("Use a Printer 1,000 times")
                    .whenStatReach(Stats.CUSTOM.get(CEIStats.PRINT.get()), MinMaxBounds.Ints.atLeast(1000))
                    .special(CDPAdvancement.TaskType.EXPERT)
                    .after(BRAND_REGISTRY));

    private static CDPAdvancement create(String id, UnaryOperator<CDPAdvancement.Builder> b) {
        return new CEIAdvancement(id, b);
    }

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CEIAdvancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registries.thenCompose(provider -> {
            PackOutput.PathProvider pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
            List<CompletableFuture<?>> futures = new ArrayList<>();

            Set<ResourceLocation> set = Sets.newHashSet();
            Consumer<AdvancementHolder> consumer = (advancement) -> {
                ResourceLocation id = advancement.id();
                if (!set.add(id))
                    throw new IllegalStateException("Duplicate advancement " + id);
                Path path = pathProvider.json(id);
                LOGGER.info("Saving advancement {}", id);
                futures.add(DataProvider.saveStable(cache, provider, Advancement.CODEC, advancement.value(), path));
            };

            for (CDPAdvancement advancement : ENTRIES)
                advancement.save(consumer, provider);

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (CDPAdvancement advancement : ENTRIES)
            advancement.provideLang(consumer);
    }

    public static void register() {}

    @Override
    public String getName() {
        return "Create: Enchantment Industry Advancements";
    }

    @CodeReference(value = AllTriggers.class, source = "create", license = "mit")
    public static class BuiltinTriggersQuickDeploy {
        private static final Map<ResourceLocation, BuiltinTrigger> triggers = new IdentityHashMap<>();

        public static BuiltinTrigger add(ResourceLocation id) {
            var instance = new BuiltinTrigger();
            triggers.put(id, instance);
            return instance;
        }

        public static void register() {
            triggers.entrySet().forEach(set -> {
                Registry.register(BuiltInRegistries.TRIGGER_TYPES, set.getKey(), set.getValue());
            });
        }
    }
}
