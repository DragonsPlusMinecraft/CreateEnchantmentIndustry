package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

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
        return be.copyTarget==null?ItemStack.EMPTY:be.copyTarget.copy();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(be.copyTarget!=null) return stack;
        else{
            if(!simulate){
                be.tooExpensive = CopyingBook.isTooExpensive(stack, CeiConfigs.SERVER.copierTankCapacity.get());
                be.copyTarget = stack;
                be.processingTicks = -1;
                be.notifyUpdate();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        var ret = be.copyTarget==null?ItemStack.EMPTY:be.copyTarget.copy();
        if(!simulate){
            be.tooExpensive = false;
            be.copyTarget = null;
            be.processingTicks = -1;
            be.notifyUpdate();
        }
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return (stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.WRITTEN_BOOK)) && stack.getCount() == 1;
    }
}
