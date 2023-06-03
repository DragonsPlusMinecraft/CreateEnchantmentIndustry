package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("getExperienceReward")
    default int invoke(Player pPlayer) {
        throw new AssertionError();
    }
}
