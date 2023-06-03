package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler")
public abstract class OpenEndFluidHandlerMixin extends FluidTank {
    public OpenEndFluidHandlerMixin(int capacity) {
        super(capacity);
    }

    @SuppressWarnings("target")
    @Final
    @Shadow(remap = false)
    OpenEndedPipe this$0;

    // Sadly, fluidStack in OpenEndedPipe#registerEffectHandler thing does not be provided as expected.
    // We intercept running before experience is handled;
    @Inject(method = "fill",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/fluid/FluidHelper;copyStackWithAmount(Lnet/minecraftforge/fluids/FluidStack;I)Lnet/minecraftforge/fluids/FluidStack;"),
            remap = false,
            cancellable = true)
    private void injected(FluidStack resource, FluidAction action, CallbackInfoReturnable<Integer> cir) {
        if(resource.getFluid() instanceof ExperienceFluid expFluid) {
            int fill = super.fill(resource, action);
            if (action.simulate())
                cir.setReturnValue(fill);
            var amount = getFluidAmount();
            if (amount != 0) {
                ((OpenEndedPipeInvoker)this$0).invokeApplyEffects(new FluidStack(expFluid, amount));
                this.setFluid(FluidStack.EMPTY);
            }
            cir.setReturnValue(fill);
        }
    }
    
}
