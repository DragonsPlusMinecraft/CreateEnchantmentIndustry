package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Enchanting {

    public static final TagKey<Item> UNENCHANTABLE =
            TagKey.create(Registries.ITEM, EnchantmentIndustry.genRL("unenchantable"));
    public static final List<Predicate<ItemStack>> UNENCHANTABLE_CONDITIONS = new ArrayList<>();

    @Nullable
    public static EnchantmentEntry getTargetEnchantment(ItemStack itemStack, boolean hyper) {
        if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
            var result = EnchantingGuideItem.getEnchantment(itemStack);
            if (result != null && hyper)
                result.setSecond(result.getSecond() + 1);
            return result;
        } else
            throw new RuntimeException("TargetItem is not an enchanting guide for blaze!");
    }
    
    @Nullable
    public static EnchantmentEntry getValidEnchantment(ItemStack itemStack, ItemStack targetItem, boolean hyper) {
        if(itemStack.is(UNENCHANTABLE)) return null;
        if(!UNENCHANTABLE_CONDITIONS.isEmpty()){
            if(UNENCHANTABLE_CONDITIONS.stream()
                    .map(itemStackPredicate -> itemStackPredicate.test(itemStack))
                    .reduce((b1,b2)->b1||b2).get()) return null;
        }

        var entry = getTargetEnchantment(targetItem, hyper);
        if (entry == null || !entry.valid())
            return null;

        ItemStack toCheck = itemStack.copy();
        var modified = EnchantmentHelper.getEnchantmentsForCrafting(toCheck);

        if (modified.keySet().contains(entry.getEnchantmentHolder()) &&
                modified.getLevel(entry.getEnchantmentHolder()) >= entry.getSecond()) {
            return null;
        }

        if (!toCheck.supportsEnchantment(entry.getEnchantmentHolder()))
            return null;

        if (!EnchantmentHelper.isEnchantmentCompatible(modified.keySet(), entry.getEnchantmentHolder()))
            return null;

        return entry;
    }

    public static void enchantItem(ItemStack itemStack, EnchantmentEntry enchantment) {
        ItemEnchantments.Mutable itemenchantments$mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(itemStack));
        itemenchantments$mutable.set(enchantment.getEnchantmentHolder(), enchantment.getSecond());
        EnchantmentHelper.setEnchantments(itemStack, itemenchantments$mutable.toImmutable());
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

    public static int rarityLevel(Rarity rarity) {
        return switch(rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case EPIC -> 4;
        };
    }

    public static int getExperienceConsumption(ItemStack itemStack, Enchantment enchantment, int level) {
        int xpLevel = enchantment.getMinCost(level) + level * rarityLevel(itemStack.getRarity());
        return expPointForNextLevel(xpLevel);
    }
    
}
