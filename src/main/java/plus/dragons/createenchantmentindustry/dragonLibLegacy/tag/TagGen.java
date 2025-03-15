package plus.dragons.createenchantmentindustry.dragonLibLegacy.tag;

/*
MIT License

Copyright (c) 2019 simibubi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.init.SafeRegistrate;

import java.util.ArrayList;
import java.util.List;

/** Originated from package com.simibubi.create.foundation.data.TagGen;
 *
 */
public class TagGen {
    private static final List<TagGen> READY_TO_GEN = new ArrayList<>();

    private SafeRegistrate registrate;
    private final List<NonNullConsumer<RegistrateTagsProvider<Block>>> blockTagGen;
    private final List<NonNullConsumer<RegistrateTagsProvider<Fluid>>> fluidTagGen;
    private final List<NonNullConsumer<RegistrateTagsProvider<Item>>> itemTagGen;
    private TagGen(SafeRegistrate registrate, List<NonNullConsumer<RegistrateTagsProvider<Block>>> blockTagGen, List<NonNullConsumer<RegistrateTagsProvider<Fluid>>> fluidTagGen, List<NonNullConsumer<RegistrateTagsProvider<Item>>> itemTagGen) {
        this.registrate = registrate;
        this.blockTagGen = blockTagGen;
        this.fluidTagGen = fluidTagGen;
        this.itemTagGen = itemTagGen;
    }

    public static void genAll(){
        for(var tagGen:READY_TO_GEN){
            for(var c:tagGen.blockTagGen){
                tagGen.registrate.addDataGenerator(ProviderType.BLOCK_TAGS,c);
            }
            for(var c:tagGen.itemTagGen){
                tagGen.registrate.addDataGenerator(ProviderType.ITEM_TAGS,c);
            }
            for(var c:tagGen.fluidTagGen){
                tagGen.registrate.addDataGenerator(ProviderType.FLUID_TAGS,c);
            }
        }
    }

    public void activate(){
        READY_TO_GEN.add(this);
    }

    public static class Builder{
        private SafeRegistrate registrate;
        private final List<NonNullConsumer<RegistrateTagsProvider<Block>>> blockTagGen = new ArrayList<>();
        private final List<NonNullConsumer<RegistrateTagsProvider<Fluid>>> fluidTagGen = new ArrayList<>();
        private final List<NonNullConsumer<RegistrateTagsProvider<Item>>> itemTagGen = new ArrayList<>();


        public Builder(SafeRegistrate registrate) {
            this.registrate = registrate;
        }

        public Builder addItemTagFactory(NonNullConsumer<RegistrateTagsProvider<Item>> consumer){
            itemTagGen.add(consumer);
            return this;
        }

        public Builder addBlockTagFactory(NonNullConsumer<RegistrateTagsProvider<Block>> consumer){
            blockTagGen.add(consumer);
            return this;
        }

        public Builder addFluidTagFactory(NonNullConsumer<RegistrateTagsProvider<Fluid>> consumer){
            fluidTagGen.add(consumer);
            return this;
        }

        public TagGen build(){
            return new TagGen(registrate,blockTagGen,fluidTagGen,itemTagGen);
        }
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_AXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
            String... path) {
        return b -> {
            for (String p : path)
                b.tag(AllTags.commonBlockTag(p));
            ItemBuilder<BlockItem, BlockBuilder<T, P>> item = b.item();
            for (String p : path)
                item.tag(AllTags.commonItemTag(p));
            return item;
        };
    }

    public static <T extends TagsProvider.TagAppender<?>> T addOptional(T appender, Mods mod, String id) {
        appender.addOptional(mod.asResource(id));
        return appender;
    }

    public static <T extends TagsProvider.TagAppender<?>> T addOptional(T appender, Mods mod, String... ids) {
        for (String id : ids) {
            appender.addOptional(mod.asResource(id));
        }
        return appender;
    }
}
