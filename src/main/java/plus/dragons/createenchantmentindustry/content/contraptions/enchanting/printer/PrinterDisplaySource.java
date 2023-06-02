package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.source.SingleLineDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import plus.dragons.createenchantmentindustry.foundation.utility.CeiLang;

public class PrinterDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceTE() instanceof PrinterBlockEntity printer))
            return EMPTY_LINE;
        if(printer.copyTarget == null){
            return CeiLang.translate("gui.goggles.printer.no_target").component();
        } else {
            if (printer.copyTarget.is(Items.WRITTEN_BOOK)) {
                var page = WrittenBookItem.getPageCount(printer.copyTarget);
                return CeiLang.builder()
                        .add(CeiLang.itemName(printer.copyTarget))
                        .text( " / ")
                        .add(CeiLang.number(page)
                                .text(" ")
                                .add(page == 1 ? CeiLang.translate("generic.unit.page") : CeiLang.translate("generic.unit.pages"))).component();
            } else if (printer.copyTarget.is(Items.ENCHANTED_BOOK)) {
                var ret = CeiLang.itemName(printer.copyTarget).text( " / ");
                var map = EnchantmentHelper.getEnchantments(printer.copyTarget);
                for (var e : map.entrySet()) {
                    Component name = e.getKey().getFullname(e.getValue());
                    ret.add(name.copy()).text(" ");
                }
                return ret.component();
            } else if (printer.copyTarget.is(Items.NAME_TAG)) {
                return CeiLang.builder()
                        .add(new TranslatableComponent(printer.copyTarget.getDescriptionId()))
                        .text(" / ")
                        .add(CeiLang.itemName(printer.copyTarget)).component();
            } else {
                return CeiLang.itemName(printer.copyTarget).component();
            }
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}
