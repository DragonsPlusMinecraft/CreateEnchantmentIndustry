package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.source.SingleLineDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class TargetEnchantmentDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceTE() instanceof BlazeEnchanterBlockEntity blazeEnchanter))
            return EMPTY_LINE;
        EnchantmentEntry entry = Enchanting.getTargetEnchantment(blazeEnchanter.targetItem, blazeEnchanter.hyper());
        if(entry == null || !entry.valid()){
            return EnchantmentIndustry.LANG.translate("gui.goggles.blaze_enchanter.invalid_target").component();
        } else return (MutableComponent) entry.getFirst().getFullname(entry.getSecond());
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}
