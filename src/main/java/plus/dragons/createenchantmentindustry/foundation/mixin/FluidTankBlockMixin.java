package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.fluids.tank.CreativeFluidTankTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.entry.ModFluids;

@Mixin(FluidTankBlock.class)
public abstract class FluidTankBlockMixin extends Block implements ITE<BasinTileEntity>, IWrenchable {
    public FluidTankBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // Support Experience Drop with Block Break
    @Inject(method = "onRemove", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"), cancellable = true)
    private void injected(BlockState state, Level world, BlockPos pos, BlockState newState, boolean var4, CallbackInfo ci) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof FluidTankTileEntity tankBE) || be instanceof CreativeFluidTankTileEntity)
            return;
        var contronllerBE = tankBE.getControllerTE();
        var fluid = contronllerBE.getFluid(0);
        var backup = fluid.copy();
        var maxSize = contronllerBE.getTotalTankSize();
        if(fluid.getFluid().isSame(ModFluids.EXPERIENCE.get().getSource())){
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
            if(maxSize==1){
                var expBall = new ExperienceOrb(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, backup.getAmount());
                world.addFreshEntity(expBall);
            } else {
                var total = maxSize * (FluidTankTileEntity.getCapacityMultiplier() - 1);
                var left = backup.getAmount()-total;
                if(left>0){
                    var expBall = new ExperienceOrb(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, left);
                    world.addFreshEntity(expBall);
                }

            }
            ci.cancel();
        }

    }
}
