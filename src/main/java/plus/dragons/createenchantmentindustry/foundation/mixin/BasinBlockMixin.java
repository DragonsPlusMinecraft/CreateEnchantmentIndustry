package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.processing.BasinBlock;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;

@Mixin(value = BasinBlock.class)
public abstract class BasinBlockMixin extends Block implements ITE<BasinTileEntity>, IWrenchable {
    public BasinBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // Support Experience Drop with Block Break
    @Inject(method = "onRemove",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/block/ITE;onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", remap = false))
    private void injected(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        withTileEntityDo(level, pos, te -> {
            var tanks = te.getTanks();
            for (var tank : tanks) {
                var fluidStack = tank.getPrimaryHandler().getFluid();
                if(fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                    expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
                }
            }
        });
    }
}
