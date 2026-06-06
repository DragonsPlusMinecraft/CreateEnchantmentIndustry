package plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy;

import com.simibubi.create.foundation.advancement.CreateAdvancement;
import java.util.function.UnaryOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = CreateAdvancement.class, remap = false)
public interface CreateAdvancementConstructor {
    @Invoker("<init>")
    static CreateAdvancement createInstance(String id, UnaryOperator<?> transform) {
        throw new AbstractMethodError();
    }
}
