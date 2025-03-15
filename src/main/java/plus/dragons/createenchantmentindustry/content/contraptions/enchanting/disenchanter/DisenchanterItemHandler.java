package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.antlr.v4.runtime.misc.NotNull;

public class DisenchanterItemHandler implements IItemHandler {
    private final DisenchanterBlockEntity be;
    private final Direction side;

    public DisenchanterItemHandler(DisenchanterBlockEntity be, Direction side) {
        this.be = be;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return be.getHeldItemStack();
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!be.getHeldItemStack().isEmpty())
            return stack;

        ItemStack disenchanted = Disenchanting.disenchantAndInsert(be, stack, simulate);
        if (!ItemStack.matches(stack, disenchanted)) {
            return disenchanted;
        }

        ItemStack returned = ItemStack.EMPTY;
        if (stack.getCount() > 1 && Disenchanting.disenchantResult(stack, be.getLevel()) != null) {
            returned = copyStackWithSize(stack, stack.getCount() - 1);
            stack = copyStackWithSize(stack, 1);
        }

        if (!simulate) {
            TransportedItemStack heldItem = new TransportedItemStack(stack);
            heldItem.prevBeltPosition = 0;
            be.setHeldItem(heldItem, side.getOpposite());
            be.notifyUpdate();
        }

        return returned;
    }

    @NotNull
    public static ItemStack copyStackWithSize(@NotNull ItemStack itemStack, int size)
    {
        if (size == 0)
            return ItemStack.EMPTY;
        ItemStack copy = itemStack.copy();
        copy.setCount(size);
        return copy;
    }

    @Override
    @NotNull
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
