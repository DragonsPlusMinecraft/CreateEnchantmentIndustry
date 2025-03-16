package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TriggerFactory {
    private final List<CriterionWithId<? extends SimpleCriterionTrigger.SimpleInstance>> triggers = new ArrayList<>();
    
    public SimpleTrigger simple(ResourceLocation resourceLocation) {
        return add(new SimpleTrigger(resourceLocation));
    }
    
    public AccumulativeTrigger accumulative(ResourceLocation resourceLocation) {
        return add(new AccumulativeTrigger(resourceLocation));
    }

    private <T extends CriterionWithId<? extends SimpleCriterionTrigger.SimpleInstance>> T add(T instance) {
        triggers.add(instance);
        return instance;
    }

    public void register() {
        triggers.forEach(trigger -> {
            Registry.register(BuiltInRegistries.TRIGGER_TYPES, trigger.getId(), trigger);
        });
    }

}
