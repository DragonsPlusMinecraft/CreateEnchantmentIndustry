package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;

public interface CriterionWithId<T extends SimpleCriterionTrigger.SimpleInstance> extends CriterionTrigger<T> {
    ResourceLocation getId();
    T instance();
}
