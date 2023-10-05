package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import java.util.ArrayList;

public class FurnaceExpExtractor implements IFluidHandler{
    final Object2IntOpenHashMap<ResourceLocation> recipesUsed;
    final AbstractFurnaceBlockEntity BE;

    public FurnaceExpExtractor(Object2IntOpenHashMap<ResourceLocation> recipesUsed, AbstractFurnaceBlockEntity BE) {
        this.recipesUsed = recipesUsed;
        this.BE = BE;
    }

    int getTotalExp() {
        AtomicDouble result = new AtomicDouble(0);
        for (Object2IntMap.Entry<ResourceLocation> entry : recipesUsed.object2IntEntrySet()) {
            BE.getLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(recipe ->
                    result.addAndGet(((AbstractCookingRecipe) recipe).getExperience() * entry.getIntValue())
            );
        }
        return (int) Math.floor(result.floatValue());
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        var total = getTotalExp();
        if (total > 0) return new FluidStack(CeiFluids.EXPERIENCE.get(), total);
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return getTotalExp();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.getFluid().isSame(CeiFluids.EXPERIENCE.get());
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (getTotalExp() == 0) {
            return FluidStack.EMPTY;
        } else if (resource.getFluid().isSame(CeiFluids.EXPERIENCE.get())) {
            var maxDrain = resource.getAmount();
            return drain(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        var total = getTotalExp();
        if (total == 0) {
            return FluidStack.EMPTY;
        } else if (maxDrain <= total) {
            if (action.execute()) recipesUsed.clear();
            return new FluidStack(CeiFluids.EXPERIENCE.get(), total);
        }
        ArrayList<Recipe<?>> allRecipes = new ArrayList<>();
        for (Object2IntMap.Entry<ResourceLocation> entry : recipesUsed.object2IntEntrySet()) {
            BE.getLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
                for(int i=0;i<entry.getIntValue();i++){
                    allRecipes.add(recipe);
                }
            });
        }
        var done = false;
        var result = 0;
        for(var recipe: allRecipes){
            if (done) {
                if (action.execute()) {
                    BE.setRecipeUsed(recipe);
                }
            } else {
                var exp = ((AbstractCookingRecipe) recipe).getExperience();
                if (exp <= maxDrain - result) {
                    result+=exp;
                } else {
                    done = true;
                    if (action.execute()) {
                        recipesUsed.clear();
                    }
                }
            }
        }
        return new FluidStack(CeiFluids.EXPERIENCE.get(), (int) Math.floor(result));
    }
}
