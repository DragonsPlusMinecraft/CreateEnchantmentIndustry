package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModAdvancements implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    public static final List<ModAdvancement> ENTRIES = new ArrayList<>();
    public static final ModAdvancement
    START = null,
    //TODO: Add advancements
    END = null;
    
    //Shortcut for builder constructor
    public static ModAdvancement.Builder builder(String id) {
        return new ModAdvancement.Builder(id);
    }
    
    //Datagen
    private final DataGenerator generator;
    
    public ModAdvancements(DataGenerator generator) {
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
