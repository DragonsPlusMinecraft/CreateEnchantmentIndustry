package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

public class DisenchantRecipe extends ProcessingRecipe<RecipeWrapper> {

    public DisenchantRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CeiRecipeTypes.DISENCHANTING, params);
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level pLevel) {
        return ingredients.get(0).test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }


    public FluidStack getResultingFluid() {
        if (fluidResults.isEmpty())
            throw new IllegalStateException("Emptying Recipe: " + id.toString() + " has no fluid output!");
        if (!fluidResults.get(0).getFluid().isSame(CeiFluids.EXPERIENCE.get().getSource()))
            throw new IllegalStateException("Illegal Recipe: " + id.toString() + " has wrong type of fluid output!");
        return fluidResults.get(0);
    }
}
