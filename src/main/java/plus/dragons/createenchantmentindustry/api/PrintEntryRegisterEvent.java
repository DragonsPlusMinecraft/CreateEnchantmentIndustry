package plus.dragons.createenchantmentindustry.api;

import net.neoforged.bus.api.Event;
import org.antlr.v4.runtime.misc.NotNull;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntries;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntry;

public class PrintEntryRegisterEvent extends Event {
    public void register(@NotNull PrintEntry printEntry){
        if(PrintEntries.ENTRIES.put(printEntry.id(),printEntry)!=null)
            throw new IllegalArgumentException(printEntry.id() + "has already been registered!");
    }

}
