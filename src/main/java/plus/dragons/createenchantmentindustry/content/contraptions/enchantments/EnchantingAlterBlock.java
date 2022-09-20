package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class EnchantingAlterBlock extends HorizontalDirectionalBlock implements IWrenchable, ITE<EnchantingAlterBlockEntity> {
    public EnchantingAlterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<EnchantingAlterBlockEntity> getTileEntityClass() {
        return EnchantingAlterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnchantingAlterBlockEntity> getTileEntityType() {
        return ModBlockEntities.BLAZE_ENCHANTING_ALTER.get();
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos blockPos, CollisionContext pContext) {
        return AllShapes.HEATER_BLOCK_SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        withTileEntityDo(worldIn, pos, te -> {
            ItemStack heldItemStack = te.getHeldItemStack();
            if (!heldItemStack.isEmpty())
                Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
            Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), te.targetItem);
        });
        worldIn.removeBlockEntity(pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(handIn);
        if (!heldItem.isEmpty())
            return InteractionResult.PASS;
        worldIn.setBlockAndUpdate(pos, AllBlocks.BLAZE_BURNER.getDefaultState()
            .setValue(BlazeBurnerBlock.FACING, state.getValue(FACING))
            .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING)
        );
        return InteractionResult.SUCCESS;
    }


    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        // TODO Advancement need more investigate
        // AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(AllBlocks.BLAZE_BURNER.get());
    }

    // TODO: When create itself change it, change it.
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }


    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        var ret = new ArrayList<ItemStack>();
        ret.add(new ItemStack(AllBlocks.BLAZE_BURNER.get()));
        return ret;
    }

    // TODO: When create itself change it, change it.
    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
    }

    // TODO: When create itself change it, change it.
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }
}
