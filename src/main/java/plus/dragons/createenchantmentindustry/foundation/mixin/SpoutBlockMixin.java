package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mixin(value = SpoutBlock.class)
public abstract class SpoutBlockMixin extends Block implements IWrenchable, ITE<SpoutTileEntity>{
    public SpoutBlockMixin(Properties pProperties) {
        super(pProperties);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        if (level instanceof ServerLevel serverLevel) {
            withTileEntityDo(level, pos, te -> {
                var fluidStack = ((SpoutTileEntityAccessor) te).getTank().getPrimaryHandler().getFluid();
                if(fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                    expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
                }
            });
        }
        level.removeBlockEntity(pos);
    }
}
