package plus.dragons.createenchantmentindustry.foundation.advancement;

import plus.dragons.createdragonlib.advancement.critereon.AccumulativeTrigger;
import plus.dragons.createdragonlib.advancement.critereon.TriggerFactory;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.ADVANCEMENT_FACTORY;

public class CeiTriggers {
    private static final TriggerFactory FACTORY = ADVANCEMENT_FACTORY.getTriggerFactory();
    
    public static final AccumulativeTrigger BOOK_PRINTED = FACTORY.accumulative(EnchantmentIndustry.genRL("book_printed"));
    public static final AccumulativeTrigger DISENCHANTED = FACTORY.accumulative(EnchantmentIndustry.genRL("disenchanted"));
    
}
