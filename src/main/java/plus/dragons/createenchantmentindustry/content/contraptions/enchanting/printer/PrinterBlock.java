package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class PrinterBlock extends Block implements IWrenchable, ITE<PrinterBlockEntity> {
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
            return onTileEntityUse(world, pos, be -> {
                if (be.copyTarget != null) {
                    player.setItemInHand(hand, be.copyTarget);
                    be.tooExpensive = false;
                    be.copyTarget = null;
                    be.processingTicks = -1;
                    be.notifyUpdate();
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            });
        } else if ((heldItem.is(Items.ENCHANTED_BOOK) || heldItem.is(Items.WRITTEN_BOOK)) && heldItem.getCount() == 1) {
            return onTileEntityUse(world, pos, be -> {
                if (be.copyTarget == null) {
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, ItemStack.EMPTY);
                } else {
                    player.setItemInHand(hand, be.copyTarget);
                }
                be.tooExpensive = CopyingBook.isTooExpensive(heldItem, CeiConfigs.SERVER.copierTankCapacity.get());
                be.copyTarget = heldItem;
                be.processingTicks = -1;
                be.notifyUpdate();
                return InteractionResult.SUCCESS;
            });
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        ITE.onRemove(state,level,pos,newState);
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
    public Class<PrinterBlockEntity> getTileEntityClass() {
        return PrinterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PrinterBlockEntity> getTileEntityType() {
        return CeiBlockEntities.PRINTER.get();
    }
}
