package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import org.slf4j.Logger;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.ArrayList;
import java.util.List;

public class CeiTriggers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<CriterionTrigger<?>> TRIGGERS = new ArrayList<>();
    public static final AccumulativeTrigger BOOK_PRINTED = add(new AccumulativeTrigger(EnchantmentIndustry.genRL("book_printed")));
    public static final AccumulativeTrigger DISENCHANTED = add(new AccumulativeTrigger(EnchantmentIndustry.genRL("disenchanted")));

    public static SimpleTrigger addSimple(String id) {
        return add(new SimpleTrigger(EnchantmentIndustry.genRL(id)));
    }

    private static <T extends CriterionTrigger<?>> T add(T instance) {
        TRIGGERS.add(instance);
        return instance;
    }

    public static void register() {
        TRIGGERS.forEach(CriteriaTriggers::register);
        LOGGER.debug("Register {} builtin triggers", TRIGGERS.size());
    }

}
