package plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy;

import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementBehaviour.class)
public interface AdvancementBehaviourAccessor {
    @Accessor(remap = false)
    UUID getPlayerId();
}
