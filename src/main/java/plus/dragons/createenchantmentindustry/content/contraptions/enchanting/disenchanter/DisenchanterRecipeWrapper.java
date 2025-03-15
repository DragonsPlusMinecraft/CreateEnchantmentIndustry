package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class DisenchanterRecipeWrapper extends RecipeWrapper {

    public DisenchanterRecipeWrapper(IItemHandlerModifiable inv) {
        super(inv);
    }

    public void setItem(int slot, ItemStack stack) {
        ((IItemHandlerModifiable) inv).setStackInSlot(slot, stack);
    }
}
