package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.RawExperienceUtil;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void create_enchantment_industry$pickupRawExperience(Player player, CallbackInfo ci) {
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        if (!RawExperienceUtil.isRawExperienceOrb(self))
            return;
        ci.cancel();
        RawExperienceUtil.pickupRawExperience(self, player, null);
    }

    @Inject(method = "canMerge(Lnet/minecraft/world/entity/ExperienceOrb;)Z", at = @At("HEAD"), cancellable = true)
    private void create_enchantment_industry$preventMixedRawExperienceMerge(ExperienceOrb other, CallbackInfoReturnable<Boolean> cir) {
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        boolean selfRaw = RawExperienceUtil.isRawExperienceOrb(self);
        boolean otherRaw = RawExperienceUtil.isRawExperienceOrb(other);
        if (selfRaw != otherRaw || (selfRaw && self.getType() != other.getType()))
            cir.setReturnValue(false);
    }

    @Inject(method = "canMerge(Lnet/minecraft/world/entity/ExperienceOrb;II)Z", at = @At("HEAD"), cancellable = true)
    private static void create_enchantment_industry$preventVanillaExperienceMergingIntoRaw(ExperienceOrb existing, int id, int value,
            CallbackInfoReturnable<Boolean> cir) {
        if (RawExperienceUtil.isRawExperienceOrb(existing))
            cir.setReturnValue(false);
    }
}
