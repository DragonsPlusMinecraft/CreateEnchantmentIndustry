package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExperienceRotorItem extends Item {
    
    public ExperienceRotorItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return true;
    }
    
    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }
    
    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return itemStack;
    }
    
}
