package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class TargetEnchantmentDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof BlazeEnchanterBlockEntity blazeEnchanter))
            return EMPTY_LINE;
        EnchantmentEntry entry = Enchanting.getTargetEnchantment(blazeEnchanter.targetItem, blazeEnchanter.hyper());
        if(entry == null || !entry.valid()){
            return EnchantmentIndustry.LANG.translate("gui.goggles.blaze_enchanter.invalid_target").component();
        } else return (MutableComponent) Enchantment.getFullname(entry.getEnchantmentHolder(), entry.getSecond());
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}
