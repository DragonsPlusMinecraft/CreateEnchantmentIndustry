package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.*;

public interface CeiTags<T, P extends RegistrateTagsProvider<T>> {
    // There is no No need to Change there to follow Create >= 0.5.0.g TagGen Change. Since support of 1.18.2 will be dropped.
    ITagManager<Block> BLOCK_TAGS = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags());
    ITagManager<Item> ITEM_TAGS = Objects.requireNonNull(ForgeRegistries.ITEMS.tags());
    ITagManager<Fluid> FLUID_TAGS = Objects.requireNonNull(ForgeRegistries.FLUIDS.tags());
    String FORGE = "forge";
    String CREATE = "create";
    
    TagKey<T> tag();
    
    boolean hasDatagen();
    
    default void datagen(P pov) {
        //NO-OP
    }
    
    static String toTagName(String enumName) {
        return enumName.replace('$', '/').toLowerCase(Locale.ROOT);
    }
    
    static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE).tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }
    
    static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE);
    }
    
    static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }
    
    static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(String namespace, String... paths) {
        return block -> {
            ItemBuilder<BlockItem, BlockBuilder<T, P>> item = block.item();
            for (String path : paths) {
                block.tag(BLOCK_TAGS.createTagKey(new ResourceLocation(namespace, path)));
                item.tag(ITEM_TAGS.createTagKey(new ResourceLocation(namespace, path)));
            }
            return item;
        };
    }
    
    static void register() {
        CreateRegistrate registrate = EnchantmentIndustry.registrate();
        Arrays.stream(BlockTag.values())
            .filter(CeiTags::hasDatagen)
            .forEach(tag -> registrate.addDataGenerator(ProviderType.BLOCK_TAGS, tag::datagen));
        Arrays.stream(ItemTag.values())
            .filter(CeiTags::hasDatagen)
            .forEach(tag -> registrate.addDataGenerator(ProviderType.ITEM_TAGS, tag::datagen));
        Arrays.stream(FluidTag.values())
            .filter(CeiTags::hasDatagen)
            .forEach(tag -> registrate.addDataGenerator(ProviderType.FLUID_TAGS, tag::datagen));
    }
    
    enum BlockTag implements CeiTags<Block, RegistrateTagsProvider<Block>> {
        ;
        
        final TagKey<Block> tag;
        final boolean datagen;
        
        BlockTag(String namespace, boolean datagen) {
            this.tag = BLOCK_TAGS.createTagKey(new ResourceLocation(namespace, toTagName(name())));
            this.datagen = datagen;
        }
        
        BlockTag(boolean datagen) {
            this(EnchantmentIndustry.ID, datagen);
        }
    
        @Override
        public TagKey<Block> tag() {
            return tag;
        }
    
        @Override
        public boolean hasDatagen() {
            return datagen;
        }
    }
    
    enum ItemTag implements CeiTags<Item, RegistrateItemTagsProvider> {
        INK_INGREDIENT(true) {
            @Override
            public void datagen(RegistrateItemTagsProvider pov) {
                pov.tag(tag).add(Items.BLACK_DYE, Items.WITHER_ROSE, Items.INK_SAC);
            }
        },
        UPRIGHT_ON_BELT(CREATE, true) {
            @Override
            public void datagen(RegistrateItemTagsProvider pov) {
                pov.tag(tag).add(Items.EXPERIENCE_BOTTLE);
            }
        };
        
        final TagKey<Item> tag;
        final boolean datagen;
    
        ItemTag(String namespace, boolean datagen) {
            this.tag = ITEM_TAGS.createTagKey(new ResourceLocation(namespace, toTagName(name())));
            this.datagen = datagen;
        }
    
        ItemTag(boolean datagen) {
            this(EnchantmentIndustry.ID, datagen);
        }
    
        @Override
        public TagKey<Item> tag() {
            return tag;
        }
    
        @Override
        public boolean hasDatagen() {
            return datagen;
        }
    }
    
    enum FluidTag implements CeiTags<Fluid, RegistrateTagsProvider<Fluid>> {
        //No experience fluid tag here as different ratios is not acceptable
        INK(FORGE, false),
        BLAZE_ENCHANTER_INPUT(false),
        PRINTER_INPUT(true) {
            @Override
            public void datagen(RegistrateTagsProvider<Fluid> pov) {
                pov.tag(tag).addTag(INK.tag);
            }
        };
        
        final TagKey<Fluid> tag;
        final boolean datagen;
    
        FluidTag(String namespace, boolean datagen) {
            this.tag = FLUID_TAGS.createTagKey(new ResourceLocation(namespace, toTagName(name())));
            this.datagen = datagen;
        }
    
        FluidTag(boolean datagen) {
            this(EnchantmentIndustry.ID, datagen);
        }
    
        @Override
        public TagKey<Fluid> tag() {
            return tag;
        }
    
        @Override
        public boolean hasDatagen() {
            return datagen;
        }
    }
    
}
