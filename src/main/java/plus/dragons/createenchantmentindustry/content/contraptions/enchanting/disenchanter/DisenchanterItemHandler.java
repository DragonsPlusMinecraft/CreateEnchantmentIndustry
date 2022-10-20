package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class DisenchanterItemHandler implements IItemHandler {
    private DisenchanterBlockEntity be;
    private Direction side;

    public DisenchanterItemHandler(DisenchanterBlockEntity be, Direction side) {
        this.be = be;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return be.getHeldItemStack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!be.getHeldItemStack()
                .isEmpty())
            return stack;

        // Build-in handling logic
        if(Disenchanting.isBuiltIn(stack)){
            return Disenchanting.handleBuiltIn(be,stack,simulate);
        }

        ItemStack returned = ItemStack.EMPTY;
        if (stack.getCount() > 1 && Disenchanting.test(stack,be.getLevel())!= Disenchanting.Type.NONE) {
            returned = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
            stack = ItemHandlerHelper.copyStackWithSize(stack, 1);
        }

        if (!simulate) {
            TransportedItemStack heldItem = new TransportedItemStack(stack);
            heldItem.prevBeltPosition = 0;
            be.setHeldItem(heldItem, side.getOpposite());
            be.notifyUpdate();
        }

        return returned;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        TransportedItemStack held = be.heldItem;
        if (held == null)
            return ItemStack.EMPTY;

        ItemStack stack = held.stack.copy();
        ItemStack extracted = stack.split(amount);
        if (!simulate) {
            be.heldItem.stack = stack;
            if (stack.isEmpty())
                be.heldItem = null;
            be.notifyUpdate();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
