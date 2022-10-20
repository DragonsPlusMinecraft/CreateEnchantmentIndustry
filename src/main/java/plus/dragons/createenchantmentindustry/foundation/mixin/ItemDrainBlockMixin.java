package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainBlock;
import com.simibubi.create.content.contraptions.fluids.actors.ItemDrainTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

@Mixin(ItemDrainBlock.class)
public abstract class ItemDrainBlockMixin extends Block implements ITE<ItemDrainTileEntity>, IWrenchable {
    public ItemDrainBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // Support Experience Drop with Block Break
    @Inject(method = "onRemove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private void injected(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        withTileEntityDo(level, pos, te -> {
            var fluidStack = ((ItemDrainTileEntityAccessor) te).getInternalTank().getPrimaryHandler().getFluid();
            if (fluidStack.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())) {
                ExperienceOrb.award(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
            }
        });
    }
}
