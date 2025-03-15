package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiComponents;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

import javax.annotation.Nullable;
import java.util.List;

public class EnchantingGuideItem extends Item implements MenuProvider {
    public EnchantingGuideItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getDisplayName() {
        return getDescription();
    }

    public InteractionResult useOn(UseOnContext pContext) {
        var level = pContext.getLevel();
        var player = pContext.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            var itemStack = pContext.getItemInHand();
            if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
                var blockPos = pContext.getClickedPos();
                var blockState = level.getBlockState(blockPos);
                var blockEntity = level.getBlockEntity(blockPos);
                if (blockState.getBlock() instanceof BlazeBurnerBlock &&
                        blockEntity instanceof BlazeBurnerBlockEntity) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(blockPos, CeiBlocks.BLAZE_ENCHANTER.getDefaultState()
                                .setValue(BlazeEnchanterBlock.FACING, level.getBlockState(blockPos).getValue(BlazeBurnerBlock.FACING))
                        );
                        if (level.getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity tileEntity) {
                            var i = itemStack.copy();
                            i.setCount(1);
                            tileEntity.setTargetItem(i);
                        }
                        AdvancementBehaviour.setPlacedBy(pContext.getLevel(), blockPos, player);
                        CeiAdvancements.BLAZES_NEW_JOB.getTrigger().trigger((ServerPlayer) player);
                        if (!player.getAbilities().instabuild)
                            itemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!world.isClientSide && player instanceof ServerPlayer sp)
                sp.openMenu(this, buf -> {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, heldItem);
                    buf.writeBoolean(true);
                });
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.create_enchantment_industry.enchanting_guide.tooltip.current_enchantment"));
        EnchantmentEntry enchantment = getEnchantment(pStack);
        if (enchantment == null) {
            pTooltipComponents.add(Component.translatable("item.create_enchantment_industry.enchanting_guide.tooltip.not_configured"));
        } else
            pTooltipComponents.add(Enchantment.getFullname(enchantment.getEnchantmentHolder(), enchantment.getSecond()));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return getEnchantment(pStack) != null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ItemStack heldItem = pPlayer.getMainHandItem();
        return new EnchantingGuideMenu(CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, heldItem, null);
    }

    @Nullable
    public static EnchantmentEntry getEnchantment(ItemStack itemStack) {
        var targetComponent = itemStack.get(CeiComponents.ENCHANTING_TARGET);
        if (targetComponent == null) return null;

        var target = ItemStack.parseOptional(ServerLifecycleHooks.getCurrentServer().registryAccess(), targetComponent);

        var enchants = EnchantmentHelper.getEnchantmentsForCrafting(target);
        if (enchants.isEmpty()) return null;

        var index = itemStack.get(CeiComponents.ENCHANTING_INDEX);
        if (index >= enchants.size()) index = 0;

        var iterator = enchants.entrySet().iterator();
        for (int i = 0; i < index; i++) iterator.next();

        var entry = iterator.next();
        return EnchantmentEntry.of(entry.getKey(), entry.getIntValue());
    }
}
