package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.TriggerFactory;

public class AdvancementFactory {
    private final String modid;
    private final AdvancementGen advancementGen;
    private final TriggerFactory triggerFactory = new TriggerFactory();
    private final Runnable preTask;

    private AdvancementFactory(String name, String modid, Runnable preTask) {
        this.modid = modid;
        this.advancementGen = new AdvancementGen(name, modid);
        this.preTask = preTask;
    }

    public static AdvancementFactory create(String name, String modid, Runnable preTask) {
        return new AdvancementFactory(name, modid, preTask);
    }

    public AdvancementHolder.Builder builder(String id) {
        return new AdvancementHolder.Builder(modid, id, triggerFactory);
    }

    public TriggerFactory getTriggerFactory() {
        return triggerFactory;
    }

    public void datagen(final GatherDataEvent event) {
        preTask.run();
        DataGenerator datagen = event.getGenerator();
        advancementGen.generator = datagen;
        datagen.addProvider(event.includeServer(), advancementGen);
    }

    public void register() {
        triggerFactory.register();
    }
}
