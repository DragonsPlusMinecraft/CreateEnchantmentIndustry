package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.ModFluids;
import plus.dragons.createenchantmentindustry.entry.ModItems;

public class Enchanting {

    public static boolean valid(ItemStack itemStack, ItemStack targetItem) {
        var enchantmentPair = getTargetEnchantment(targetItem);
        var enchantment = enchantmentPair.getFirst();
        if (!enchantment.canEnchant(itemStack)) return false;
        var map = EnchantmentHelper.getEnchantments(itemStack);
        for (var e : map.entrySet()) {
            if (!e.getKey().isCompatibleWith(enchantment))
                return false;
            if (e.getKey() == enchantment && e.getValue() >= enchantmentPair.getSecond())
                return false;
        }
        return true;
    }

    public static Pair<FluidStack, ItemStack> enchant(ItemStack stack, ItemStack targetItem, boolean simulate) {
        var enchantment = getTargetEnchantment(targetItem);
        var resultingFluid = new FluidStack(ModFluids.EXPERIENCE.get().getSource(), getExperienceFromEnchantment(enchantment));
        var resultingItem = enchantItem(stack, enchantment, simulate);
        return Pair.of(resultingFluid, resultingItem);
    }

    public static ItemStack enchantItem(ItemStack itemStack, Pair<Enchantment, Integer> enchantment, boolean simulate) {
        var result = itemStack.copy();
        var map = EnchantmentHelper.getEnchantments(result);
        map.put(enchantment.getFirst(), enchantment.getSecond());
        EnchantmentHelper.setEnchantments(map, result);
        if (simulate) return result;
        else {
            itemStack = result;
            return itemStack;
        }
    }

    private static Pair<Enchantment, Integer> getTargetEnchantment(ItemStack itemStack) {
        if (itemStack.is(ModItems.ENCHANTING_GUIDE_FOR_BLAZE.get()))
            return EnchantingGuideItem.getEnchantment(itemStack);
        else throw new RuntimeException("TargetItem is not an enchanting guide for blaze!");
    }

    private static int getExperienceFromEnchantment(Pair<Enchantment, Integer> enchantmentPair) {
        return enchantmentPair.getFirst().getMaxCost(enchantmentPair.getSecond());
    }
}
