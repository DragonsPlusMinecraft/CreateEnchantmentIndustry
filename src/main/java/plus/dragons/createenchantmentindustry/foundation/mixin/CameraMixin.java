package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.entry.ModTags;

@Mixin(Camera.class)
public class CameraMixin {
    
    @Shadow private boolean initialized;
    
    @Shadow private BlockGetter level;
    
    @Shadow @Final private BlockPos.MutableBlockPos blockPosition;
    
    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    private void enchantmentIndustry$handleInkFogType(CallbackInfoReturnable<FogType> cir) {
        if (this.initialized) {
            FluidState fluidstate = this.level.getFluidState(this.blockPosition);
            if (fluidstate.is(ModTags.ModFluidTags.INK.tag())) {
                cir.setReturnValue(FogType.POWDER_SNOW);
            }
        }
    }
    
}
