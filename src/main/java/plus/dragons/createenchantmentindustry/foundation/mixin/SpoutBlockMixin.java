package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import com.simibubi.create.foundation.block.ITE;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import plus.dragons.createenchantmentindustry.entry.ModFluids;

@Mixin(SpoutBlock.class)
public abstract class SpoutBlockMixin extends Block implements IWrenchable, ITE<SpoutTileEntity>{
    public SpoutBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        withTileEntityDo(worldIn, pos, te -> {
            var tank = ((SpoutTileEntityAccessor) te).getTank();
            if(tank.getPrimaryHandler().getFluid().getFluid().isSame(ModFluids.EXPERIENCE.get().getSource())){
                var expBall = new ExperienceOrb(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tank.getPrimaryHandler().getFluid().getAmount());
                worldIn.addFreshEntity(expBall);
            }
        });
        worldIn.removeBlockEntity(pos);
    }
}
