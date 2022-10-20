package plus.dragons.createenchantmentindustry.foundation.data.advancement;


import plus.dragons.createdragonlib.advancement.AccumulativeTrigger;
import plus.dragons.createdragonlib.advancement.ModTriggerFactory;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;


public class CeiTriggers {
    public static final AccumulativeTrigger BOOK_PRINTED = ModTriggerFactory.addAccumulative(EnchantmentIndustry.genRL("book_printed"));
    public static final AccumulativeTrigger DISENCHANTED = ModTriggerFactory.addAccumulative(EnchantmentIndustry.genRL("disenchanted"));

    public static void register() {
    }

}
