package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemDrainBlockEntity.class)
public interface ItemDrainBlockEntityAccessor {

    @Accessor(remap = false)
    SmartFluidTankBehaviour getInternalTank();

}
