package plus.dragons.createenchantmentindustry.api;

import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntries;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntry;

public class PrintEntryRegisterEvent extends Event {
    public void register(@NotNull PrintEntry printEntry){
        if(PrintEntries.ENTRIES.put(printEntry.id(),printEntry)!=null)
            throw new IllegalArgumentException(printEntry.id() + "has already been registered!");
    }

}
