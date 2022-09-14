package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerTileEntity;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.ModBlocks;
import plus.dragons.createenchantmentindustry.entry.ModContainerTypes;
import plus.dragons.createenchantmentindustry.entry.ModItems;

@Mod.EventBusSubscriber(modid = EnchantmentIndustry.MOD_ID)
public class EnchantingGuideItem extends Item implements MenuProvider {
    public EnchantingGuideItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getDisplayName() {
        return getDescription();
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ItemStack heldItem = pPlayer.getMainHandItem();
        return new EnchantingGuideMenu(ModContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, heldItem);
    }

    public static Pair<Enchantment,Integer> getEnchantment(ItemStack itemStack){
        var tag = itemStack.getOrCreateTag().get("target");
        if(tag == null)
            return null;
        var book = ItemStack.of((CompoundTag) tag);
        var index = itemStack.getOrCreateTag().getInt("index");
        var result = EnchantmentHelper.getEnchantments(book).entrySet().stream().toList().get(index);
        return Pair.of(result.getKey(),result.getValue());
    }

    @SubscribeEvent
    public static void transform(PlayerInteractEvent.RightClickBlock event){
        var player = event.getPlayer();
        var level = player.level;
        if(!event.isCanceled() && !level.isClientSide()){
            var itemStack = player.getItemInHand(event.getHand());
            if(itemStack.is(ModItems.ENCHANTING_GUIDE_FOR_BLAZE.get())){
                var blockPos = event.getHitVec().getBlockPos();
                if(EnchantingGuideItem.getEnchantment(itemStack)!=null && level.getBlockEntity(blockPos) instanceof BlazeBurnerTileEntity){
                    level.setBlockAndUpdate(blockPos, ModBlocks.BLAZE_ENCHANTING_ALTER.getDefaultState());
                    if(level.getBlockEntity(blockPos) instanceof EnchantingAlterBlockEntity enchantingAlterBlockEntity){
                        var i = itemStack.copy();
                        i.setCount(1);
                        enchantingAlterBlockEntity.setTargetItem(i);
                    }
                }
            }
        }
    }
}
