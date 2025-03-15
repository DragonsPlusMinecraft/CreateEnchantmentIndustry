package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.ComparatorUtil;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlazeEnchanterBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<BlazeEnchanterBlockEntity> {

    public static final EnumProperty<HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", HeatLevel.class);
    public static final MapCodec<BlazeEnchanterBlock> CODEC = simpleCodec(BlazeEnchanterBlock::new);

    public BlazeEnchanterBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, HeatLevel.SMOULDERING));
    }

    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<BlazeEnchanterBlockEntity> getBlockEntityClass() {
        return BlazeEnchanterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeEnchanterBlockEntity> getBlockEntityType() {
        return CeiBlockEntities.BLAZE_ENCHANTER.get();
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT_LEVEL, FACING);
    }

    @Override
    public Item asItem() {
        return AllBlocks.BLAZE_BURNER.get().asItem();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos blockPos, CollisionContext pContext) {
        return AllShapes.HEATER_BLOCK_SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state,level,pos,newState);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack pStack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack heldItem;

        if(handIn==InteractionHand.OFF_HAND)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (player.isCreative()) {
            heldItem = player.getItemInHand(handIn).copy();
        } else {
            heldItem = player.getItemInHand(handIn);
        }

        if (player.isShiftKeyDown() && heldItem.isEmpty()){
            if(!player.level().isClientSide()){
                if(player.level().getBlockEntity(pos) instanceof BlazeEnchanterBlockEntity blazeEnchanter){
                    withBlockEntityDo(player.level(), pos,
                            toolbox -> {
                                if (player instanceof ServerPlayer serverPlayer) {
                                    serverPlayer.openMenu(blazeEnchanter, buf -> {
                                        ItemStack.STREAM_CODEC.encode(buf, blazeEnchanter.targetItem);
                                        buf.writeBoolean(false);
                                        buf.writeBlockPos(pos);
                                    });
                                }
                            });
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (!heldItem.isEmpty()){
            return onBlockEntityUseItemOn(worldIn, pos, te -> {
                if(heldItem.is(CeiItems.ENCHANTING_GUIDE.get())){
                    if (!worldIn.isClientSide) {
                        var target = te.targetItem.copy();
                        te.targetItem = heldItem;
                        if(!player.getAbilities().instabuild)
                            player.setItemInHand(handIn, target);
                        te.notifyUpdate();
                    }
                    return ItemInteractionResult.SUCCESS;
                } else if(Enchanting.getValidEnchantment(heldItem, te.targetItem, te.hyper()) != null) {
                    ItemStack heldItemStack = te.getHeldItemStack();
                    if (heldItemStack.isEmpty()) {
                        if (!worldIn.isClientSide) {
                            te.heldItem = new TransportedItemStack(heldItem);
                            if(!player.getAbilities().instabuild)
                                player.setItemInHand(handIn, ItemStack.EMPTY);
                            te.notifyUpdate();
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                    return ItemInteractionResult.FAIL;
                } else if (AllItems.GOGGLES.isIn(heldItem)) {
                    if (te.goggles)
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    te.goggles = true;
                    te.notifyUpdate();
                    return ItemInteractionResult.SUCCESS;
                }
                else return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            });
        } else {
            return onBlockEntityUseItemOn(worldIn, pos, te -> {
                ItemStack heldItemStack = te.getHeldItemStack();
                if (!heldItemStack.isEmpty()) {
                    if (!worldIn.isClientSide) {
                        te.heldItem = null;
                        player.setItemInHand(handIn, heldItemStack);
                        te.notifyUpdate();
                    }
                    return ItemInteractionResult.SUCCESS;
                } if (!te.goggles)
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                te.goggles = false;
                te.notifyUpdate();
                return ItemInteractionResult.SUCCESS;
            });
        }
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (world instanceof ServerLevel) {
            if (player != null)
                player.level().setBlockAndUpdate(pos, AllBlocks.BLAZE_BURNER.getDefaultState()
                        .setValue(BlazeBurnerBlock.FACING, state.getValue(FACING))
                        .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING));
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(AllBlocks.BLAZE_BURNER.get());
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }


    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        var ret = new ArrayList<ItemStack>();
        ret.add(new ItemStack(AllBlocks.BLAZE_BURNER.get()));
        return ret;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
    }

    @Override
    public boolean isPathfindable(BlockState pState, PathComputationType pPathComputationType) {
        return false;
    }

    public static int getLight(BlockState state) {
        HeatLevel level = state.getValue(HEAT_LEVEL);
        return switch (level) {
            case SEETHING -> 15;
            case KINDLED -> 11;
            case SMOULDERING -> 7;
        };
    }

    public enum HeatLevel implements StringRepresentable {
        SMOULDERING, KINDLED, SEETHING,;

        public static HeatLevel byIndex(int index) {
            return values()[index];
        }

        public HeatLevel nextActiveLevel() {
            return byIndex(ordinal() % (values().length - 1) + 1);
        }

        public boolean isAtLeast(HeatLevel heatLevel) {
            return this.ordinal() >= heatLevel.ordinal();
        }

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
}
