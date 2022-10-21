package plus.dragons.createenchantmentindustry.content.contraptions.enchanting;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
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
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.ModConfigs;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CopierBlock extends Block implements IWrenchable, ITE<CopierBlockEntity> {
    public CopierBlock(Properties pProperties) {
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
        ret.add(CeiBlocks.COPIER.asStack());
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
        } else if ((heldItem.is(Items.ENCHANTED_BOOK) || heldItem.is(Items.WRITTEN_BOOK)) && heldItem.getCount()==1) {
            return onTileEntityUse(world, pos, be -> {
                if (be.copyTarget == null) {
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, ItemStack.EMPTY);
                } else {
                    player.setItemInHand(hand, be.copyTarget);
                }
                be.tooExpensive = CopyingBook.isTooExpensive(heldItem, ModConfigs.SERVER.copierTankCapacity.get());
                be.copyTarget = heldItem;
                be.processingTicks = -1;
                be.notifyUpdate();
                return InteractionResult.SUCCESS;
            });
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        withTileEntityDo(worldIn, pos, te -> {
            ItemStack heldItemStack = te.copyTarget;
            if (heldItemStack != null)
                Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
            var tank = te.tank.getPrimaryHandler();
            if (tank.getFluid().getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource())) {
                var expBall = new ExperienceOrb(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tank.getFluid().getAmount());
                worldIn.addFreshEntity(expBall);
            }
        });
        worldIn.removeBlockEntity(pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<CopierBlockEntity> getTileEntityClass() {
        return CopierBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CopierBlockEntity> getTileEntityType() {
        return CeiBlockEntities.COPIER.get();
    }
}
