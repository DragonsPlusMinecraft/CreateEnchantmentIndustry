package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.antlr.v4.runtime.misc.NotNull;

public class PrinterTargetItemHandler implements IItemHandler {
    PrinterBlockEntity be;

    public PrinterTargetItemHandler(PrinterBlockEntity be) {
        this.be = be;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return be.getCopyTarget();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(!be.getCopyTarget().isEmpty()) return stack;
        if(!isItemValid(slot,stack)) return stack; // Prevent strange crash problem from happening. See #170 log. Chute does not check item validity before insertion.
        else{
            if(!simulate){
                be.setCopyTarget(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        var ret = be.getCopyTarget().copy();
        if(!simulate){
            be.setCopyTarget(ItemStack.EMPTY);
        }
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return Printing.match(stack)!=null;
    }
}
