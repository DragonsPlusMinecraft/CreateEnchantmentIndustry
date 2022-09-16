package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerTileEntity;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
        if (player.isShiftKeyDown()) {
            var itemStack = pContext.getItemInHand();
            if (itemStack.is(ModItems.ENCHANTING_GUIDE_FOR_BLAZE.get())) {
                var blockPos = pContext.getClickedPos();
                if (EnchantingGuideItem.getEnchantment(itemStack) != null && level.getBlockEntity(blockPos) instanceof BlazeBurnerTileEntity) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(blockPos, ModBlocks.BLAZE_ENCHANTING_ALTER.getDefaultState());
                        if (level.getBlockEntity(blockPos) instanceof EnchantingAlterBlockEntity enchantingAlterBlockEntity) {
                            var i = itemStack.copy();
                            i.setCount(1);
                            enchantingAlterBlockEntity.setTargetItem(i);
                        }
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
        pTooltipComponents.add(new TranslatableComponent("create_enchantment_industry.tooltip.guide"));
        var enchantment = getEnchantment(pStack);
        if (enchantment == null) {
            pTooltipComponents.add(new TranslatableComponent("create_enchantment_industry.tooltip.guide_not_configured"));
        } else
            pTooltipComponents.add(enchantment.getFirst().getFullname(enchantment.getSecond()));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ItemStack heldItem = pPlayer.getMainHandItem();
        return new EnchantingGuideMenu(ModContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, heldItem);
    }

    public static Pair<Enchantment, Integer> getEnchantment(ItemStack itemStack) {
        var tag = itemStack.getOrCreateTag().get("target");
        if (tag == null)
            return null;
        var book = ItemStack.of((CompoundTag) tag);
        var index = itemStack.getOrCreateTag().getInt("index");
        var result = EnchantmentHelper.getEnchantments(book).entrySet().stream().toList().get(index);
        return Pair.of(result.getKey(), result.getValue());
    }
}
