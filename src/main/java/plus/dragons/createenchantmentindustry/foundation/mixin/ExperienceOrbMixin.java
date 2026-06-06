package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.RawExperienceUtil;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Shadow
    public int value;

    @Shadow
    public int count;

    @Shadow
    public abstract int repairPlayerItems(Player player, int value);

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void create_enchantment_industry$pickupRawExperience(Player player, CallbackInfo ci) {
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        if (!RawExperienceUtil.isRawExperienceOrb(self))
            return;
        ci.cancel();
        if (self.level().isClientSide || player.takeXpDelay != 0)
            return;
        int rawValue = value;
        if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, self)))
            return;

        player.takeXpDelay = 2;
        player.take(self, 1);
        int left = repairPlayerItems(player, rawValue);
        if (left > 0)
            RawExperienceUtil.addRawExperience(player, left);

        --count;
        if (count == 0)
            self.discard();
    }
}
