package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class PrinterDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof PrinterBlockEntity printer))
            return EMPTY_LINE;
        if(printer.getCopyTarget().isEmpty()){
            return EnchantmentIndustry.LANG.translate("gui.goggles.printer.no_target").component();
        } else {
            return printer.printEntry.getDisplaySourceContent(printer.getCopyTarget());
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}
