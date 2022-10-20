package plus.dragons.createenchantmentindustry.content.contraptions.enchanting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;


public class CopyingBook {
    public static int INK_CONSUMPTION_PER_PAGE_COPYING = 5;
    public static int EXPERIENCE_MULTIPLIER_FOR_COPYING = 10;


    public static boolean valid(ItemStack tested) {
        return tested.is(Items.BOOK);
    }

    public static int getRequiredAmountForItem(ItemStack target) {
        if (target.is(Items.WRITTEN_BOOK))
            return WrittenBookItem.getPageCount(target) * INK_CONSUMPTION_PER_PAGE_COPYING;
        else if (target.is(Items.ENCHANTED_BOOK))
            return getExperienceFromItem(target) * EXPERIENCE_MULTIPLIER_FOR_COPYING;
        else return -1;
    }

    public static boolean isCorrectInt(ItemStack target, FluidStack fluidStack) {
        if (target.is(Items.ENCHANTED_BOOK) && fluidStack.getFluid().isSame(CeiFluids.EXPERIENCE.get())) return true;
        else return target.is(Items.WRITTEN_BOOK) && fluidStack.getFluid().isSame(CeiFluids.INK.get());
    }

    public static ItemStack print(ItemStack target, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        stack.shrink(1);
        availableFluid.shrink(requiredAmount);
        if (target.is(Items.WRITTEN_BOOK)) {
            var ret = target.copy();
            target.getOrCreateTag().putInt("generation", 0);
            return ret;
        } else return target.copy();
    }

    public static boolean isTooExpensive(ItemStack target, int limit) {
        if (target.is(Items.WRITTEN_BOOK))
            return WrittenBookItem.getPageCount(target) * INK_CONSUMPTION_PER_PAGE_COPYING > limit;
        else if (target.is(Items.ENCHANTED_BOOK))
            return getExperienceFromItem(target) * EXPERIENCE_MULTIPLIER_FOR_COPYING > limit;
        return false;
    }

    private static int getExperienceFromItem(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantments(itemStack).entrySet().stream().map(enchantmentEntry -> enchantmentEntry.getKey().getMaxCost(enchantmentEntry.getValue())).reduce(0, Integer::sum);
    }


}
