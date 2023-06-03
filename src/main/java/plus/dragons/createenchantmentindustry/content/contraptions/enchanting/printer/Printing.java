package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.AllItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;


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
    public static boolean isCorrectInk(PrintEntry printEntry, FluidStack fluidStack) {
        return fluidStack.getFluid().isSame(printEntry.requiredInkType());
    }

    public static ItemStack print(PrintEntry printEntry, ItemStack target, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        stack.shrink(1);
        availableFluid.shrink(requiredAmount);
        return printEntry.print(target);
    }

    public static boolean isTooExpensive(PrintEntry printEntry, ItemStack target, int limit) {
        return printEntry.isTooExpensive(target,limit);
    }



}
