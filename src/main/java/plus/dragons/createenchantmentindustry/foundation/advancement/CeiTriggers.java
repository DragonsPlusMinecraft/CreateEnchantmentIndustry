package plus.dragons.createenchantmentindustry.foundation.advancement;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.ADVANCEMENT_FACTORY;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.AccumulativeTrigger;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.TriggerFactory;

public class CeiTriggers {
    private static final TriggerFactory FACTORY = ADVANCEMENT_FACTORY.getTriggerFactory();

    public static final AccumulativeTrigger BOOK_PRINTED = FACTORY.accumulative(EnchantmentIndustry.genRL("book_printed"));
    public static final AccumulativeTrigger DISENCHANTED = FACTORY.accumulative(EnchantmentIndustry.genRL("disenchanted"));
}
