package plus.dragons.createenchantmentindustry.api.event;

import com.simibubi.create.foundation.item.CreateItemGroupBase;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class FillCreateItemGroupEvent extends Event {
    private final CreateItemGroupBase itemGroup;
    private final NonNullList<ItemStack> items;
    private final Map<Item, List<ItemStack>> insertions = new IdentityHashMap<>();
    
    public FillCreateItemGroupEvent(CreateItemGroupBase itemGroup, NonNullList<ItemStack> items) {
        this.itemGroup = itemGroup;
        this.items = items;
    }
    
    public CreateItemGroupBase getItemGroup() {
        return itemGroup;
    }
    
    public NonNullList<ItemStack> getItems() {
        return items;
    }
    
    public void addInsertion(ItemLike target, ItemStack stack) {
        insertions.computeIfAbsent(target.asItem(), $ -> new ArrayList<>()).add(stack);
    }
    
    public void addInsertions(ItemLike target, Collection<ItemStack> stacks) {
        insertions.computeIfAbsent(target.asItem(), $ -> new ArrayList<>()).addAll(stacks);
    }
    
    @ApiStatus.Internal
    public void apply() {
        ListIterator<ItemStack> it = items.listIterator();
        while(it.hasNext()) {
            Item item = it.next().getItem();
            if (insertions.containsKey(item)) {
                for (var inserted : insertions.get(item)) {
                    it.add(inserted);
                }
                insertions.remove(item);
            }
        }
    }
    
}
