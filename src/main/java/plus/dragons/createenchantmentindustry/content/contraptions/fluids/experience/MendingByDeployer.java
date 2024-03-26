package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

public class MendingByDeployer {
    
    public static boolean canItemBeMended(ItemStack stack) {
        return stack.isDamaged() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack) > 0;
    }
    
    public static int getRequiredAmountForItem(ItemStack stack) {
        return Mth.ceil(stack.getDamageValue() / stack.getXpRepairRatio());
    }
    public static int getNewXp(int xpAmount, ItemStack stack) {
        int requiredAmount = getRequiredAmountForItem(stack);
        int afterXp = 0;

        if(requiredAmount % 2 != 0) {
            requiredAmount -= 1;
        }

        if(requiredAmount == 1) {
            afterXp = xpAmount;
        }
        else if(requiredAmount > 1 && requiredAmount < xpAmount) {
            afterXp = xpAmount - requiredAmount;
        }

        return afterXp;

    }
    @Nullable
    public static ItemStack mendItem(int xpAmount, ItemStack stack) {
        int requiredAmount = getRequiredAmountForItem(stack);
        int damage = stack.getDamageValue();
        if(requiredAmount % 2 != 0) {
            requiredAmount -= 1;
        }
        damage -= xpAmount * 2;
        stack.setDamageValue(damage);
        return stack;
    }


}
