package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement;

import com.google.common.collect.Sets;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class AdvancementGen implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String name;
    private final String modid;
    DataGenerator generator;

    AdvancementGen(String name, String modid) {
        this.name = name;
        this.modid = modid;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = this.generator.getPackOutput().getOutputFolder();

        return CompletableFuture.runAsync(() -> {
            Set<ResourceLocation> set = Sets.newHashSet();
            Consumer<Advancement> consumer = advancement -> {
                if (!set.add(advancement.getId()))
                    throw new IllegalStateException("Duplicate advancement " + advancement.getId());
                Path advancementPath = path.resolve("data/"
                        + advancement.getId().getNamespace() + "/advancements/"
                        + advancement.getId().getPath() + ".json");
                DataProvider.saveStable(cache, advancement.deconstruct().serializeToJson(), advancementPath);
            };
            var advancements = AdvancementHolder.ENTRIES_MAP.get(modid);
            if (advancements != null)
                for (var advancement : advancements) {
                    advancement.save(consumer);
                }
        });
    }

    @Override
    public String getName() {
        return name + " Advancements";
    }
}
