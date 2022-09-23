package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.processing.BasinBlock;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.entry.ModFluids;

@Mixin(BasinBlock.class)
public abstract class BasinBlockMixin extends Block implements ITE<BasinTileEntity>, IWrenchable {
    public BasinBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // Support Experience Drop with Block Break
    @Inject(method = "onRemove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private void injected(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        withTileEntityDo(worldIn, pos, te -> {
            var tanks = te.getTanks();
            for(var tank:tanks){
                if(tank.getPrimaryHandler().getFluid().getFluid().isSame(ModFluids.EXPERIENCE.get().getSource())){
                    var expBall = new ExperienceOrb(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tank.getPrimaryHandler().getFluid().getAmount());
                    worldIn.addFreshEntity(expBall);
                }
            }
        });
    }
}
