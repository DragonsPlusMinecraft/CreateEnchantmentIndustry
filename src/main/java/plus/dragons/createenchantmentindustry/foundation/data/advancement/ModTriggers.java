package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.advancement.CriterionTriggerBase;
import net.minecraft.advancements.CriteriaTriggers;
import org.slf4j.Logger;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.ArrayList;
import java.util.List;

public class ModTriggers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<CriterionTriggerBase<?>> TRIGGERS = new ArrayList<>();
    
    public static SimpleTrigger addSimple(String id) {
        return add(new SimpleTrigger(EnchantmentIndustry.genRL(id)));
    }
    
    private static <T extends CriterionTriggerBase<?>> T add(T instance) {
        TRIGGERS.add(instance);
        return instance;
    }
    
    public static void register() {
        TRIGGERS.forEach(CriteriaTriggers::register);
        LOGGER.debug("Register {} builtin triggers", TRIGGERS.size());
    }
    
}
