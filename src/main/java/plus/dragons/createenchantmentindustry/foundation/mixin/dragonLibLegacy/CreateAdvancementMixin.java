package plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy;

import com.simibubi.create.foundation.advancement.CreateAdvancement;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import org.antlr.v4.runtime.misc.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.AdvancementHolder;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.CreateAdvancementAccess;

@Mixin(value = CreateAdvancement.class, remap = false)
@Implements(@Interface(iface = CreateAdvancementAccess.class, prefix = "createDragonLib$", remap = Interface.Remap.NONE))
public class CreateAdvancementMixin {
    @Nullable
    private AdvancementHolder createDragonLib$advancement = null;

    public void createDragonLib$fromAdvancementHolder(@NotNull AdvancementHolder advancement) {
        this.createDragonLib$advancement = advancement;
    }

    @Inject(method = "isAlreadyAwardedTo", at = @At("HEAD"), cancellable = true)
    private void createDragonLibAlreadyAwardedTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (createDragonLib$advancement != null) {
            cir.setReturnValue(createDragonLib$advancement.isAlreadyAwardedTo(player));
        }
    }

    @Inject(method = "awardTo", at = @At("HEAD"), cancellable = true)
    private void createDragonLibAwardTo(Player player, CallbackInfo ci) {
        if (createDragonLib$advancement != null) {
            createDragonLib$advancement.awardTo(player);
            ci.cancel();
        }
    }
}
