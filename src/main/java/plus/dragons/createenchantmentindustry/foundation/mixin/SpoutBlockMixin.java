package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import plus.dragons.createenchantmentindustry.entry.CeiFluids;

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
        if (!(level instanceof ServerLevel serverLevel) || !state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        withTileEntityDo(level, pos, te -> {
            var fluidStack = ((SpoutTileEntityAccessor) te).getTank().getPrimaryHandler().getFluid();
            if(fluidStack.getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())){
                ExperienceOrb.award(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
            }
        });
        level.removeBlockEntity(pos);
    }
}
