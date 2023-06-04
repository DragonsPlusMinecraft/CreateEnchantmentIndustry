package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.ComparatorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class PrinterBlock extends Block implements IWrenchable, IBE<PrinterBlockEntity> {
    public PrinterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext pContext) {
        return AllShapes.SPOUT;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        var ret = new ArrayList<ItemStack>();
        ret.add(CeiBlocks.PRINTER.asStack());
        return ret;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !AllBlocks.BASIN.has(worldIn.getBlockState(pos.below()));
    }
    
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult blockRayTraceResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return onBlockEntityUse(world, pos, be -> {
                if (!be.getCopyTarget().isEmpty()) {
                    player.setItemInHand(hand, be.getCopyTarget());
                    be.setCopyTarget(ItemStack.EMPTY);
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            });
        }
        var copy = heldItem.copy();
        copy.setCount(1);
        if(Printing.match(copy)!=null){
            return onBlockEntityUse(world, pos, be -> {
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
                if (!be.getCopyTarget().isEmpty()) {
                    if(!player.getAbilities().instabuild){
                        player.setItemInHand(hand, be.getCopyTarget());
                    }
                    else player.addItem(be.getCopyTarget());
                }
                be.setCopyTarget(copy);
                return InteractionResult.SUCCESS;
            });
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state,level,pos,newState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return ComparatorUtil.levelOfSmartFluidTank(level, pos);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<PrinterBlockEntity> getBlockEntityClass() {
        return PrinterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PrinterBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.PRINTER.get();
    }
}
