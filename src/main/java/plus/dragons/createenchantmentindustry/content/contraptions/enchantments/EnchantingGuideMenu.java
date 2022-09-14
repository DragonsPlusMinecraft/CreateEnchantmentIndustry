package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.foundation.gui.container.GhostItemContainer;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class EnchantingGuideMenu extends GhostItemContainer<ItemStack> {
    private static Component NO_ENCHANTMENT = new TranslatableComponent("tooltip.create_enchantment_industry.no_avaliable_enchantment");

    // TODO need index switcher
    public int index;
    public SelectionScrollInput scrollInput;
    public Label scrollInputLabel;

    public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
    }

    private void regenEnchantmentListData() {
        index = 0;
        genEnchantmentListData();
    }

    private void genEnchantmentListData() {
        var map = EnchantmentHelper.getEnchantments(this.getSlot(36).getItem());
        List<Component> options = new ArrayList<>();
        if (map.isEmpty())
            options.add(NO_ENCHANTMENT);
        else
            options.addAll(map.entrySet().stream().map(entry -> entry.getKey().getFullname(entry.getValue())).toList());
        scrollInput.forOptions(options);
        scrollInput.setState(index);
    }

    @Override
    protected void init(Inventory inv, ItemStack contentHolderIn) {
        super.init(inv, contentHolderIn);
        this.index = contentHolderIn.getOrCreateTag().getInt("index");
        scrollInput = new SelectionScrollInput(46, 10, 80, 30);
        scrollInputLabel = new Label(49, 14, Components.immutableEmpty()).withShadow();
        scrollInput.calling(index -> this.index = index)
                .writingTo(scrollInputLabel);
        genEnchantmentListData();
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
        if (tag.contains("target"))
            ghostInventory.setStackInSlot(0, ItemStack.of((CompoundTag) tag.get("target")));
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
    }


    static class EnchantedBookSlot extends SlotItemHandler {

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
                ItemStack stackInSlot = ghostInventory.getStackInSlot(0)
                        .copy();
                setCarried(stackInSlot);
            }
        } else if (getSlot(36).mayPlace(held) || held.isEmpty()) {
            ghostInventory.setStackInSlot(0, held.copy());
            getSlot(slotId).setChanged();
            regenEnchantmentListData();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (index < 36) {
            ItemStack stackToInsert = playerInventory.getItem(index);
            if (getSlot(36).mayPlace(stackToInsert)) {
                ItemStack copy = stackToInsert.copy();
                ghostInventory.insertItem(0, copy, false);
                getSlot(36).setChanged();
                regenEnchantmentListData();
            }
        } else {
            ghostInventory.extractItem(0, 1, false);
            getSlot(index).setChanged();
            regenEnchantmentListData();
        }
        return ItemStack.EMPTY;
    }
}
