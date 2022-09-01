package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.foundation.gui.container.GhostItemContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class EnchantingGuideMenu extends GhostItemContainer<ItemStack> {

    // TODO need index switcher
    private int index;


    public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);

    }

    @Override
    protected void init(Inventory inv, ItemStack contentHolderIn) {
        super.init(inv, contentHolderIn);
        this.index = contentHolderIn.getOrCreateTag().getInt("index");
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(1);
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    protected void initAndReadInventory(ItemStack contentHolder) {
        super.initAndReadInventory(contentHolder);
        var tag = contentHolder.getOrCreateTag();
        if(tag.contains("target"))
        ghostInventory.setStackInSlot(0,ItemStack.of((CompoundTag) tag.get("target")));
    }

    @Override
    protected ItemStack createOnClient(FriendlyByteBuf extraData) {
        return extraData.readItem();
    }


    @Override
    protected void addSlots() {
        // TODO Coordinates waiting for texture
        addPlayerSlots(46, 48);
        this.addSlot(new EnchantedBookSlot(ghostInventory, 0, 46, 18));
    }

    @Override
    protected void saveData(ItemStack contentHolder) {
        var tag = contentHolder.getOrCreateTag();
        tag.put("target", ghostInventory.getStackInSlot(0).serializeNBT());
        tag.putInt("index",index);
    }

    static class EnchantedBookSlot extends SlotItemHandler{

        public EnchantedBookSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack pStack) {
            return pStack.is(Items.ENCHANTED_BOOK) && !EnchantmentHelper.getEnchantments(pStack).isEmpty();
        }
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId < 36) {
            super.clicked(slotId, dragType, clickTypeIn, player);
            return;
        }
        if (clickTypeIn == ClickType.THROW)
            return;

        ItemStack held = getCarried();
        if (clickTypeIn == ClickType.CLONE) {
            if (player.isCreative() && held.isEmpty()) {
                ItemStack stackInSlot = ghostInventory.getStackInSlot(36)
                        .copy();
                setCarried(stackInSlot);
                return;
            }
            return;
        }

        if(getSlot(36).mayPlace(held)){
            ghostInventory.setStackInSlot(36, held.copy());
            getSlot(slotId).setChanged();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (index < 36) {
            ItemStack stackToInsert = playerInventory.getItem(index);
            for (int i = 0; i < ghostInventory.getSlots(); i++) {
                ItemStack stack = ghostInventory.getStackInSlot(i);
                if (!allowRepeats() && ItemHandlerHelper.canItemStacksStack(stack, stackToInsert))
                    break;
                if (stack.isEmpty()) {
                    ItemStack copy = stackToInsert.copy();
                    copy.setCount(1);
                    ghostInventory.insertItem(i, copy, false);
                    getSlot(i + 36).setChanged();
                    break;
                }
            }
        } else {
            ghostInventory.extractItem(index - 36, 1, false);
            getSlot(index).setChanged();
        }
        return ItemStack.EMPTY;
    }
}
