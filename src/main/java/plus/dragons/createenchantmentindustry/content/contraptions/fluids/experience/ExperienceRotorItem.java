package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExperienceRotorItem extends Item {
    public ExperienceRotorItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
}
