package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Disenchanting {

    public enum Type{
        BUILTIN,
        DISENCHANT,
        RECIPE,
        NONE
    }

    static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

    private static final Map<Integer, Integer> EXPECTED_VALUE_CACHE = new HashMap<>();

    public static Type test(ItemStack itemStack, Level level) {
        if(isBuiltIn(itemStack)) return Type.BUILTIN;
        if(!EnchantmentHelper.getEnchantments(itemStack).keySet().stream().filter(enchantment->!enchantment.isCurse()).collect(Collectors.toList()).isEmpty()) return Type.DISENCHANT;
        wrapper.setItem(0, itemStack);
        if(CeiRecipeTypes.DISENCHANTING.find(wrapper, level).isPresent()) return Type.RECIPE;
        return Type.NONE;
    }

    // Produce result only. Do not modify stack.
    // stack always has count of 1.
    public static Pair<FluidStack, ItemStack> disenchant(Type type, ItemStack stack, Level level) {
        if(type==Type.DISENCHANT) {
            FluidStack resultingFluid = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), getExperienceFromItem(stack));
            ItemStack resultingItem = disenchantItem(stack);
            return Pair.of(resultingFluid, resultingItem);
        }
        else if(type==Type.RECIPE) {
            wrapper.setItem(0, stack);
            var r = (DisenchantRecipe) CeiRecipeTypes.DISENCHANTING.find(wrapper, level).get();
            var i = r.getResultItem().copy();
            var f = r.getResultingFluid().copy();
            return Pair.of(f,i);
        }
        else if(type==Type.BUILTIN) throw new IllegalArgumentException("BUilT-IN DISENCHANTING Type requires special handling and it should not be passed in this method.");
        throw new IllegalArgumentException("NONE DISENCHANTING Type cannot be handled.");
    }

    public static ItemStack disenchantItem(ItemStack itemStack) {
        var enchants = new HashMap<>(EnchantmentHelper.getEnchantments(itemStack));
        enchants.entrySet().removeIf(enchant->!enchant.getKey().isCurse());
        var noCurse = enchants.isEmpty();
        if (itemStack.is(Items.ENCHANTED_BOOK)) {
            if(noCurse){
                return Items.BOOK.getDefaultInstance();
            } else {
                var ret = Items.ENCHANTED_BOOK.getDefaultInstance();
                EnchantmentHelper.setEnchantments(enchants,ret);
                return ret;
            }
        } else {
            ItemStack ret = itemStack.copy();
            EnchantmentHelper.setEnchantments(enchants, ret);
            return ret;
        }
    }

    public static boolean isBuiltIn(ItemStack itemStack){
        return itemStack.is(AllItems.EXP_NUGGET.get());
    }

    public static ItemStack handleBuiltIn(DisenchanterBlockEntity be, ItemStack itemStack, boolean simulated){
        if(itemStack.is(AllItems.EXP_NUGGET.get())){
            var tank = be.getInternalTank();
            tank.allowInsertion();
            var fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), itemStack.getCount() * 3);
            int inserted = tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) / 3;
            ItemStack ret = itemStack.copy();
            if(!simulated){
                fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), inserted * 3);
                tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            }
            ret.shrink(inserted);
            tank.forbidInsertion();
            return ret;


        } else throw new IllegalArgumentException("ItemStack " + itemStack + " is not built-in disenchanting item!");
    }

    /**
     * Returns the total amount of XP stored in the enchantments of this stack.
     * Curse is also countable.
     */
    private static int getExperienceFromItem(ItemStack itemStack) {
        int l = EnchantmentHelper.getEnchantments(itemStack).entrySet().stream().map(enchantmentEntry -> enchantmentEntry.getKey().getMinCost(enchantmentEntry.getValue())).reduce(0, Integer::sum);
        return l + expectedValue(l);
    }

    private static int expectedValue(int i) {
        if (i == 0) return 0;
        EXPECTED_VALUE_CACHE.computeIfAbsent(i, integer -> {
            float ret = 0;
            for (int a = 1; a < integer; a++)
                ret += ((float) a / i);
            return (int) Math.ceil(ret);
        });
        return EXPECTED_VALUE_CACHE.get(i);
    }


}
