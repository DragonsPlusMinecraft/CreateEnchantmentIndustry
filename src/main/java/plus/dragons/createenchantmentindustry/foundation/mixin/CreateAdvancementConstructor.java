package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.foundation.advancement.CreateAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.UnaryOperator;

@Mixin(value = CreateAdvancement.class, remap = false)
public interface CreateAdvancementConstructor {
    
    @Invoker("<init>")
    static CreateAdvancement createInstance(String id, UnaryOperator<?> transform) {
        throw new AbstractMethodError();
    }
    
}
