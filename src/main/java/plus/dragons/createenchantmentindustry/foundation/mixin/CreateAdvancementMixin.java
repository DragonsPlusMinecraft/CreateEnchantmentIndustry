package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.foundation.advancement.CreateAdvancement;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.ModdedCreateAdvancement;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.CeiAdvancement;

@Mixin(value = CreateAdvancement.class, remap = false)
@Implements(@Interface(iface = ModdedCreateAdvancement.class, prefix = "enchantmentIndustry$", remap = Interface.Remap.NONE))
public class CreateAdvancementMixin {
    
    @Nullable private CeiAdvancement enchantmentIndustry$advancement = null;
    
    public void enchantmentIndustry$fromModAdvancement(CeiAdvancement advancement) {
        this.enchantmentIndustry$advancement = advancement;
    }
    
    @Inject(method = "isAlreadyAwardedTo", at = @At("HEAD"), cancellable = true)
    private void enchantmentIndustryIsAlreadyAwardedTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if(enchantmentIndustry$advancement != null) {
            cir.setReturnValue(enchantmentIndustry$advancement.isAlreadyAwardedTo(player));
        }
    }
    
    @Inject(method = "awardTo", at = @At("HEAD"), cancellable = true)
    private void enchantmentIndustryAwardTo(Player player, CallbackInfo ci) {
        if(enchantmentIndustry$advancement != null) {
            enchantmentIndustry$advancement.awardTo(player);
            ci.cancel();
        }
    }
    
}
