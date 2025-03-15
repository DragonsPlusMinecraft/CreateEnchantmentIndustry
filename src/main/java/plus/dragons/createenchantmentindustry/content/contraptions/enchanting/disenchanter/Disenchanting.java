package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

public class Disenchanting {

    private static final DisenchanterRecipeWrapper WRAPPER = new DisenchanterRecipeWrapper(new ItemStackHandler(1));

    public static ItemStack disenchantAndInsert(DisenchanterBlockEntity be, ItemStack itemStack, boolean simulate) {
        Level level = be.getLevel();
        if (level == null)
            return itemStack;
        WRAPPER.setItem(0, itemStack);
        return CeiRecipeTypes.DISENCHANTING.<RecipeWrapper, DisenchantRecipe>find(WRAPPER, be.getLevel())
                .map(recipe -> {
                    if (!recipe.value().hasNoResult())
                        return itemStack;
                    var tank = be.getInternalTank();
                    tank.allowInsertion();
                    int amount = recipe.value().getExperience();
                    var fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), itemStack.getCount() * amount);
                    int inserted = tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) / amount;
                    ItemStack ret = itemStack.copy();
                    if (!simulate) {
                        fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), inserted * amount);
                        tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    }
                    ret.shrink(inserted);
                    tank.forbidInsertion();
                    return ret;
                }).orElse(itemStack);
    }

    // Produce result only. Do not modify stack.
    // stack always has count of 1.
    @Nullable
    public static Pair<FluidStack, ItemStack> disenchantResult(ItemStack itemStack, Level level) {
        if (EnchantmentHelper.getEnchantmentsForCrafting(itemStack).keySet().stream().anyMatch(enchantment -> !enchantment.is(EnchantmentTags.CURSE))) {
            var xp =
                    new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), getDisenchantExperience(itemStack));
            ItemStack result = disenchant(itemStack);
            return Pair.of(xp, result);
        }
        WRAPPER.setItem(0, itemStack);
        var recipe = CeiRecipeTypes.DISENCHANTING.<RecipeWrapper, DisenchantRecipe>find(WRAPPER, level).orElse(null);
        if (recipe != null && !recipe.value().hasNoResult()) {
            var xp = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), recipe.value().getExperience());
            var result = recipe.value().getResultItem(level.registryAccess()).copy();
            return Pair.of(xp, result);
        }
        return null;
    }

    public static ItemStack disenchant(ItemStack itemStack) {
        ItemStack pItem = itemStack.copy();
        ItemEnchantments itemenchantments = EnchantmentHelper.updateEnchantments(
                pItem, p_330066_ -> p_330066_.removeIf(p_344368_ -> !p_344368_.is(EnchantmentTags.CURSE))
        );
        if (pItem.is(Items.ENCHANTED_BOOK) && itemenchantments.isEmpty()) {
            pItem = pItem.transmuteCopy(Items.BOOK);
        }

        int i = 0;

        for (int j = 0; j < itemenchantments.size(); j++) {
            i = AnvilMenu.calculateIncreasedRepairCost(i);
        }

        pItem.set(DataComponents.REPAIR_COST, i);
        return pItem;
    }

    private static int getDisenchantExperience(ItemStack itemStack) {
        int xp = EnchantmentHelper.getEnchantmentsForCrafting(itemStack)
                .entrySet().stream()
                .filter(entry -> !entry.getKey().is(EnchantmentTags.CURSE))
                .map(entry -> entry.getKey().value().getMinCost(entry.getIntValue()))
                .reduce(0, Integer::sum);
        return xp == 0 ? 0 : Mth.ceil(xp * 0.75);
    }

}
