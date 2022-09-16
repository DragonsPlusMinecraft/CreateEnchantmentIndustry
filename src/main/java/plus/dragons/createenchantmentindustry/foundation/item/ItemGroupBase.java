package plus.dragons.createenchantmentindustry.foundation.item;

import com.simibubi.create.content.AllSections;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

public abstract class ItemGroupBase extends CreativeModeTab {

    public ItemGroupBase(String id) {
        super(EnchantmentIndustry.MOD_ID + "." + id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemList(NonNullList<ItemStack> items) {
        addItems(items, true);
        addBlocks(items);
        addItems(items, false);
    }

    @OnlyIn(Dist.CLIENT)
    public void addBlocks(NonNullList<ItemStack> items) {
        for (RegistryEntry<? extends Block> entry : getBlocks()) {
            Block def = entry.get();
            Item item = def.asItem();
            if (item != Items.AIR)
                def.fillItemCategory(this, items);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        for (RegistryEntry<? extends Item> entry : getItems()) {
            Item item = entry.get();
            if (item instanceof BlockItem)
                continue;
            ItemStack stack = new ItemStack(item);
            BakedModel model = itemRenderer.getModel(stack, null, null, 0);
            if (model.isGui3d() != specialItems)
                continue;
            item.fillItemCategory(this, items);
        }
    }

    protected Collection<RegistryEntry<Block>> getBlocks() {
        return getSections().stream()
                .flatMap(s -> EnchantmentIndustry.registrate()
                        .getAll(s, Registry.BLOCK_REGISTRY)
                        .stream())
                .collect(Collectors.toList());
    }

    protected Collection<RegistryEntry<Item>> getItems() {
        return getSections().stream()
                .flatMap(s -> EnchantmentIndustry.registrate()
                        .getAll(s, Registry.ITEM_REGISTRY)
                        .stream())
                .collect(Collectors.toList());
    }

    protected EnumSet<AllSections> getSections() {
        return EnumSet.allOf(AllSections.class);
    }
}
