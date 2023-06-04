package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;


public class Printing {

    @Nullable
    public static PrintEntry match(ItemStack toPrint){
        for(var entry:PrintEntries.ENTRIES.values()){
            if(entry.match(toPrint)) return entry;
        } return null;
    }
    public static boolean valid(PrintEntry printEntry, ItemStack printTarget, ItemStack tested) {
        return printEntry.valid(printTarget,tested);
    }

    public static int getRequiredAmountForItem(PrintEntry printEntry, ItemStack target) {
        return printEntry.requiredInkAmount(target);
    }
    
    @SuppressWarnings("deprecation") //Fluid Tags are still useful for mod interaction
    public static boolean isCorrectInk(PrintEntry printEntry, FluidStack fluidStack, ItemStack target) {
        return fluidStack.getFluid().isSame(printEntry.requiredInkType(target));
    }

    public static ItemStack print(PrintEntry printEntry, ItemStack target, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        var copy = stack.copy();
        copy.setCount(1);
        stack.shrink(1);
        availableFluid.shrink(requiredAmount);
        return printEntry.print(target,copy);
    }

    public static boolean isTooExpensive(PrintEntry printEntry, ItemStack target, int limit) {
        return printEntry.isTooExpensive(target,limit);
    }



}
