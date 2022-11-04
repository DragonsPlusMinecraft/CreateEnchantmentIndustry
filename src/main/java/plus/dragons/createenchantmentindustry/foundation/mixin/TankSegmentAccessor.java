package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmartFluidTankBehaviour.TankSegment.class)
public interface TankSegmentAccessor {
    
    @Accessor(remap = false)
    SmartFluidTank getTank();
    
}