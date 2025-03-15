package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import javax.annotation.Nullable;

public class MendingBySpout {
    
    public static boolean canItemBeMended(Level world, ItemStack stack) {
        return stack.isDamaged() && stack.getEnchantmentLevel(world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.MENDING)) > 0;
    }
    
    public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
        if (!(CeiFluids.EXPERIENCE.is(availableFluid.getFluid()) && canItemBeMended(world, stack)))
            return -1;
        return Math.min(availableFluid.getAmount(), Mth.ceil(stack.getDamageValue() / stack.getXpRepairRatio()));
    }
    
    @Nullable
    public static ItemStack mendItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        if (!(CeiFluids.EXPERIENCE.is(availableFluid.getFluid()) && canItemBeMended(world, stack)))
            return null;
        ItemStack result = stack.split(1);
        availableFluid.shrink(requiredAmount);
        int damage = result.getDamageValue();
        damage -= Math.min((int) (requiredAmount * result.getXpRepairRatio()), damage);
        result.setDamageValue(damage);
        return result;
    }
    
}
