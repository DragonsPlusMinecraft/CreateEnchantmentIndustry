package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.simibubi.create.foundation.advancement.CriterionTriggerBase;
import net.minecraft.advancements.CriteriaTriggers;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.LinkedList;
import java.util.List;

public class ModTriggers {
    
    private static final List<CriterionTriggerBase<?>> TRIGGERS = new LinkedList<>();
    
    public static SimpleTrigger addSimple(String id) {
        return add(new SimpleTrigger(EnchantmentIndustry.genRL(id)));
    }
    
    private static <T extends CriterionTriggerBase<?>> T add(T instance) {
        TRIGGERS.add(instance);
        return instance;
    }
    
    public static void register() {
        TRIGGERS.forEach(CriteriaTriggers::register);
    }
    
}
