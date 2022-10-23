package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class Enchanting {

    @Nullable
    public static EnchantmentEntry getTargetEnchantment(ItemStack itemStack, boolean hyper) {
        if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
            var result = EnchantingGuideItem.getEnchantment(itemStack);
            if (!hyper || result == null)
                return result;
            else {
                var enchantment = result.getFirst();
                int level = result.getSecond() + 1;
                return EnchantmentEntry.of(enchantment, level);
            }
        } else
            throw new RuntimeException("TargetItem is not an enchanting guide for blaze!");
    }
    
    @Nullable
    public static EnchantmentEntry getValidEnchantment(ItemStack itemStack, ItemStack targetItem, boolean hyper) {
        var entry = getTargetEnchantment(targetItem, hyper);
        if (entry == null || !entry.valid())
            return null;
        var enchantment = entry.getFirst();
        if (!enchantment.canEnchant(itemStack))
            return null;
        int level = entry.getSecond();
        var map = EnchantmentHelper.getEnchantments(itemStack);
        for (var e : map.entrySet()) {
            if (!e.getKey().isCompatibleWith(enchantment))
                return null;
            if (e.getKey() == enchantment && e.getValue() >= entry.getSecond() + level)
                return null;
        }
        return entry;
    }

    public static void enchantItem(ItemStack itemStack, Pair<Enchantment, Integer> enchantment) {
        var map = EnchantmentHelper.getEnchantments(itemStack);
        map.put(enchantment.getFirst(), enchantment.getSecond());
        EnchantmentHelper.setEnchantments(map, itemStack);
    }
    
    public static int expPointFromLevel(int level) {
        if (level > 31) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else {
            return level > 16
                ? (int) (2.5 * level * level - 40.5 * level + 360)
                : level * level + 6 * level;
        }
    }
    
    public static int expPointForNextLevel(int level) {
        if (level > 30) {
            return 9 * level - 158;
        } else {
            return level > 15
                ? 5 * level -38
                : 2 * level + 7;
        }
    }

    public static int rarityLevel(Enchantment.Rarity rarity) {
        return switch(rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case VERY_RARE -> 4;
        };
    }

    public static int getExperienceConsumption(Enchantment enchantment, int level) {
        int xpLevel = enchantment.getMinCost(level) + level * rarityLevel(enchantment.getRarity());
        return expPointForNextLevel(xpLevel);
    }
    
}
