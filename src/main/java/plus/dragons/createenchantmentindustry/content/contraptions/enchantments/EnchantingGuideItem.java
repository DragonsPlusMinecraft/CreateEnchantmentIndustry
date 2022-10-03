package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerTileEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.ModBlocks;
import plus.dragons.createenchantmentindustry.entry.ModContainerTypes;
import plus.dragons.createenchantmentindustry.entry.ModItems;
import plus.dragons.createenchantmentindustry.foundation.utility.ModLang;

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
            if (itemStack.is(ModItems.ENCHANTING_GUIDE.get())) {
                var blockPos = pContext.getClickedPos();
                var blockState = level.getBlockState(blockPos);
                var blockEntity = level.getBlockEntity(blockPos);
                if (EnchantingGuideItem.getEnchantment(itemStack) != null &&
                    blockState.getBlock() instanceof BlazeBurnerBlock &&
                    blockEntity instanceof BlazeBurnerTileEntity)
                {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(blockPos, ModBlocks.BLAZE_ENCHANTER.getDefaultState()
                            .setValue(BlazeEnchanterBlock.FACING, level.getBlockState(blockPos).getValue(BlazeBurnerBlock.FACING))
                        );
                        if (level.getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity blazeEnchanterBlockEntity) {
                            var i = itemStack.copy();
                            i.setCount(1);
                            blazeEnchanterBlockEntity.setTargetItem(i);
                        }
                        AdvancementBehaviour.setPlacedBy(pContext.getLevel(), blockPos, player);
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
            if (!world.isClientSide && player instanceof ServerPlayer)
                NetworkHooks.openGui((ServerPlayer) player, this, buf -> {
                    buf.writeItem(heldItem);
                });
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(ModLang.translate("tooltip.guide_header").component());
        var enchantment = getEnchantment(pStack);
        if (enchantment == null) {
            pTooltipComponents.add(ModLang.translate("tooltip.guide_not_configured").component());
        } else
            pTooltipComponents.add(enchantment.getFirst().getFullname(enchantment.getSecond()));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ItemStack heldItem = pPlayer.getMainHandItem();
        return new EnchantingGuideMenu(ModContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, heldItem);
    }

    @Nullable
    public static Pair<Enchantment, Integer> getEnchantment(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag == null || !tag.contains("target", Tag.TAG_COMPOUND))
            return null;
        var target = (CompoundTag) tag.get("target");
        if (target == null)
            return null;
        var book = ItemStack.of(target);
        var enchantments = List.copyOf(EnchantmentHelper.getEnchantments(book).entrySet());
        if (enchantments.isEmpty())
            return null;
        var index = tag.getInt("index");
        var result = enchantments.get(index);
        return Pair.of(result.getKey(), result.getValue());
    }
}
