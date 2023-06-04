package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

import java.util.List;

public interface PrintEntry {

    ResourceLocation id();

    boolean match(ItemStack toPrint);

    boolean valid(ItemStack target, ItemStack tested);

    int requiredInkAmount(ItemStack target);

    default Fluid requiredInkType(ItemStack target) {
        return CeiFluids.EXPERIENCE.get();
    }

    default ItemStack print(ItemStack target, ItemStack material){
        return target.copy();
    }

    boolean isTooExpensive(ItemStack target, int limit);

    void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target);

    MutableComponent getDisplaySourceContent(ItemStack target);
}
