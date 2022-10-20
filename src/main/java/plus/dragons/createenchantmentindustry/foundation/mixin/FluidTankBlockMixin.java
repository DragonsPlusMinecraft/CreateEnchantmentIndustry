package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

@Mixin(FluidTankBlock.class)
public abstract class FluidTankBlockMixin extends Block implements ITE<BasinTileEntity>, IWrenchable {
    public FluidTankBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // Support Experience Drop with Block Break
    @Inject(method = "onRemove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"), cancellable = true)
    private void injected(BlockState state, Level level, BlockPos pos, BlockState newState, boolean var4, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FluidTankTileEntity tankBE) || be instanceof CreativeFluidTankTileEntity)
            return;
        var controllerBE = tankBE.getControllerTE();
        var fluid = controllerBE.getFluid(0);
        var backup = fluid.copy();
        var maxSize = controllerBE.getTotalTankSize();
        if (fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())) {
            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
            if (maxSize == 1) {
                ExperienceOrb.award(serverLevel, VecHelper.getCenterOf(pos), backup.getAmount());
            } else {
                var total = maxSize * (FluidTankTileEntity.getCapacityMultiplier() - 1);
                var leftover = backup.getAmount() - total;
                if (leftover > 0) {
                    ExperienceOrb.award(serverLevel, VecHelper.getCenterOf(pos), leftover);
                }
            }
            ci.cancel();
        }
    }
}
