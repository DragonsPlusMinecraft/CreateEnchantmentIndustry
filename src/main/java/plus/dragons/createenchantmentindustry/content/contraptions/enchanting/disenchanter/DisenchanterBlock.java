package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class DisenchanterBlock extends Block implements IWrenchable, ITE<DisenchanterBlockEntity> {

    public DisenchanterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<DisenchanterBlockEntity> getTileEntityClass() {
        return DisenchanterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DisenchanterBlockEntity> getTileEntityType() {
        return CeiBlockEntities.DISENCHANTER.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(handIn);
        if (heldItem.isEmpty())
            return InteractionResult.PASS;
        return onTileEntityUse(worldIn, pos, te -> {
            ItemStack disenchanted = Disenchanting.disenchantAndInsert(te, heldItem, false);
            if (!ItemStack.matches(disenchanted, heldItem)) {
                player.setItemInHand(handIn, disenchanted);
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
            if (te.getHeldItemStack().isEmpty()) {
                if (!worldIn.isClientSide) {
                    te.heldItem = new TransportedItemStack(heldItem);
                    player.setItemInHand(handIn, ItemStack.EMPTY);
                    te.notifyUpdate();
                }
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
            return InteractionResult.FAIL;
        });
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos blockPos, CollisionContext pContext) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;
        if (level instanceof ServerLevel serverLevel) {
            withTileEntityDo(level, pos, te -> {
                ItemStack heldItemStack = te.getHeldItemStack();
                if(!heldItemStack.isEmpty())
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
                var tank = te.getInternalTank().getPrimaryHandler();
                var fluidStack = tank.getFluid();
                if(fluidStack.getFluid() instanceof ExperienceFluid expFluid) {
                    expFluid.drop(serverLevel, VecHelper.getCenterOf(pos), fluidStack.getAmount());
                }
            });
        }
        level.removeBlockEntity(pos);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        var ret = new ArrayList<ItemStack>();
        ret.add(CeiBlocks.DISENCHANTER.asStack());
        return ret;
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

}
