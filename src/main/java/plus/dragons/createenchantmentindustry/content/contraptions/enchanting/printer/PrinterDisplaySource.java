package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.source.SingleLineDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

public class PrinterDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceTE() instanceof PrinterBlockEntity printer))
            return EMPTY_LINE;
        if(printer.copyTarget == null){
            return EnchantmentIndustry.LANG.translate("gui.goggles.printer.no_target").component();
        } else {
            if (printer.copyTarget.is(Items.WRITTEN_BOOK)) {
                var page = WrittenBookItem.getPageCount(printer.copyTarget);
                return LANG.builder()
                        .add(LANG.itemName(printer.copyTarget))
                        .text( " / ")
                        .add(LANG.number(page)
                                .text(" ")
                                .add(page == 1 ? LANG.translate("generic.unit.page") : LANG.translate("generic.unit.pages"))).component();
            } else if (printer.copyTarget.is(Items.ENCHANTED_BOOK)) {
                var ret = LANG.itemName(printer.copyTarget).text( " / ");
                var map = EnchantmentHelper.getEnchantments(printer.copyTarget);
                for (var e : map.entrySet()) {
                    Component name = e.getKey().getFullname(e.getValue());
                    ret.add(name.copy()).text(" ");
                }
                return ret.component();
            } else if (printer.copyTarget.is(Items.NAME_TAG)) {
                return LANG.builder()
                        .add(Component.translatable(printer.copyTarget.getDescriptionId()))
                        .text(" / ")
                        .add(LANG.itemName(printer.copyTarget)).component();
            } else {
                return LANG.itemName(printer.copyTarget).component();
            }
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}
