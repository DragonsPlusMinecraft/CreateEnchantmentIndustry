package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.ModFluids;

import java.util.HashMap;
import java.util.Map;

public class Disenchanting {

    private static Map<Integer,Integer> EXPECTED_VALUE_CACHE = new HashMap<>();

    public static boolean valid(ItemStack itemStack){
        return !EnchantmentHelper.getEnchantments(itemStack).isEmpty();
    }

    public static Pair<FluidStack, ItemStack> disenchant(ItemStack stack, boolean simulate) {
        FluidStack resultingFluid = new FluidStack(ModFluids.EXPERIENCE.get().getSource(),getExperienceFromItem(stack));
        ItemStack resultingItem = disenchantItem(stack,simulate);
        return Pair.of(resultingFluid, resultingItem);
    }

    public static ItemStack disenchantItem(ItemStack itemStack, boolean simulate){
        if(itemStack.is(Items.ENCHANTED_BOOK)){
            if(simulate) return Items.BOOK.getDefaultInstance();
            else{
                itemStack = Items.BOOK.getDefaultInstance();
                return itemStack;
            }
        } else {
            ItemStack ret;
            if(simulate) ret=itemStack.copy();
            else ret = itemStack;
            EnchantmentHelper.setEnchantments(new HashMap<>(),ret);
            return ret;
        }
    }

    /**
     * Returns the total amount of XP stored in the enchantments of this stack.
     * Curse is also countable.
     */
    private static int getExperienceFromItem(ItemStack itemStack) {
        int l = EnchantmentHelper.getEnchantments(itemStack).entrySet().stream().map(enchantmentEntry -> enchantmentEntry.getKey().getMinCost(enchantmentEntry.getValue())).reduce(0,Integer::sum);
        return l + expertedValue(l);
    }

    private static int expertedValue(int i){
        if(i==0) return 0;
        EXPECTED_VALUE_CACHE.computeIfAbsent(i,integer -> {
            float ret = 0;
            for(int a=1;a<integer;a++)
                ret += ((float) a/i);
            return (int) Math.ceil(ret);
        });
        return EXPECTED_VALUE_CACHE.get(i);
    }
}
