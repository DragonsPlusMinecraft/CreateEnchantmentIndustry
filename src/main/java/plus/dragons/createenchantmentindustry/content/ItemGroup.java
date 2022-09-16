package plus.dragons.createenchantmentindustry.content;

import com.simibubi.create.content.AllSections;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.entry.ModItems;
import plus.dragons.createenchantmentindustry.foundation.item.ItemGroupBase;

import java.util.EnumSet;

public class ItemGroup extends ItemGroupBase {

    public ItemGroup() {
        super("base");
    }

    @Override
    protected EnumSet<AllSections> getSections() {
        return EnumSet.complementOf(EnumSet.of(AllSections.PALETTES));
    }

    @Override
    public ItemStack makeIcon() {
        return ModItems.ENCHANTING_GUIDE_FOR_BLAZE.asStack();
    }
}
