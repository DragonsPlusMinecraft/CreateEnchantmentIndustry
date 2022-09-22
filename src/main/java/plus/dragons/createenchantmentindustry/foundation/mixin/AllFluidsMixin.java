package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.AllFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.entry.ModTags;

@Mixin(value = AllFluids.class, remap = false)
public class AllFluidsMixin {
    
    @Inject(method = "getLavaInteraction", at = @At("HEAD"), cancellable = true)
    private static void enchantmentIndustry$handleInkLavaInteraction(FluidState fluidState, CallbackInfoReturnable<BlockState> cir) {
        if(fluidState.is(ModTags.ModFluidTags.INK.tag())) {
            cir.setReturnValue(Blocks.BLACKSTONE.defaultBlockState());
        }
    }
    
}
