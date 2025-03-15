package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement;

import com.google.common.collect.Sets;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

class AdvancementGen implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String name;
    private final String modid;
    DataGenerator generator;
    CompletableFuture<HolderLookup.Provider> provider;

    AdvancementGen(String name, String modid) {
        this.name = name;
        this.modid = modid;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.provider.thenCompose(provider -> {
            PackOutput.PathProvider pathProvider = generator.getPackOutput().createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
            List<CompletableFuture<?>> futures = new ArrayList<>();

            Set<ResourceLocation> set = Sets.newHashSet();
            Consumer<net.minecraft.advancements.AdvancementHolder> consumer = (advancement) -> {
                ResourceLocation id = advancement.id();
                if (!set.add(id))
                    throw new IllegalStateException("Duplicate advancement " + id);
                Path path = pathProvider.json(id);
                LOGGER.info("Saving advancement {}", id);
                futures.add(DataProvider.saveStable(cache, provider, Advancement.CODEC, advancement.value(), path));
            };

            for (var advancement : AdvancementHolder.ENTRIES_MAP.get(modid))
                advancement.save(consumer, provider);

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return name + " Advancements";
    }

}
