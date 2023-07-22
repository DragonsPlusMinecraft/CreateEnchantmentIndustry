package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;

@Mixin(value = BasinBlockEntity.class)
public abstract class BasinBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation {

    @Shadow(remap = false)
    private Couple<SmartFluidTankBehaviour> tanks;
    public BasinBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Support Experience Drop with Block Break
    @Inject(method = "destroy",
            at = @At(value = "RETURN"), remap = false)
    private void injected(CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        for (var tank : tanks) {
            var fluidStack = tank.getPrimaryHandler().getFluid();
            if(fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                expFluid.drop(serverLevel, VecHelper.getCenterOf(getBlockPos()), fluidStack.getAmount());
            }
        }
    }
}
