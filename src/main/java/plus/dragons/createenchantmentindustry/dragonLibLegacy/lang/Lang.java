package plus.dragons.createenchantmentindustry.dragonLibLegacy.lang;

import java.util.Locale;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

/*
 * MIT License
 * Copyright (c) 2019 simibubi
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * General utility class for text processing and localization. <br>
 * Due to the fact that the methods are modid specific, each mod should create its own instance. <br>
 * Originated from: {@link //com.simibubi.create.foundation.utility.Lang}
 */
public class Lang {
    private final String modid;

    public Lang(String modid) {
        this.modid = modid;
    }

    /**
     * Shortcut for {@link String#toLowerCase()}
     * 
     * @param name the String
     * @return the {@code String}, converted to lowercase.
     */
    public String asId(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Convert's the name of an enum instance to match {@link ResourceLocation}'s path. <br>
     * Use "$" in the enum name for "/" <br>
     * 
     * @param anEnum the Enum
     * @return the name of the enum, converted to lowercase.
     */
    public String enumId(Enum<?> anEnum) {
        return anEnum.name().toLowerCase(Locale.ROOT).replace('$', '/');
    }

    /**
     * Same with {@link Lang#asId(String)}, but removes the trailing "s" for plural words.
     * 
     * @param name the String of a plural word
     * @return the {@code String}, converted to lowercase and removed the trailing "s".
     */
    public String nonPluralId(String name) {
        String asId = asId(name);
        return asId.endsWith("s") ? asId.substring(0, asId.length() - 1) : asId;
    }

    /**
     * Create's a {@link LangBuilder} using {@link Lang#modid}
     * 
     * @return the LangBuilder
     */
    public LangBuilder builder() {
        return new LangBuilder(modid);
    }

    /**
     * Create's a {@link LangBuilder} with the name of a given {@link BlockState}.
     * 
     * @param state the {@link BlockState}
     * @return the LangBuilder
     */
    public LangBuilder blockName(BlockState state) {
        return builder().add(state.getBlock().getName());
    }

    /**
     * Create's a {@link LangBuilder} with the name of a given {@link ItemStack}
     * 
     * @param stack the {@link ItemStack}
     * @return the LangBuilder
     */
    public LangBuilder itemName(ItemStack stack) {
        return builder().add(stack.getHoverName().copy());
    }

    /**
     * Create's a {@link LangBuilder} with {@link Lang#modid} and append the name of a given {@link FluidStack}
     * 
     * @param stack the {@link FluidStack}
     * @return the LangBuilder
     */
    public LangBuilder fluidName(FluidStack stack) {
        return builder().add(stack.getDisplayName().copy());
    }

    /**
     * Create's a {@link LangBuilder} with a localized tooltip of the given {@link Item}. <br>
     * For an item with id "foo:bar", the key of the tooltip should be: "item.foo.bar.tooltip.suffix"
     * 
     * @param item   the Item
     * @param suffix a suffix for the key for different content
     * @param args   args for the tooltip {@link MutableComponent}
     * @return the LangBuilder
     */
    public LangBuilder tooltip(Item item, String suffix, Object... args) {
        return builder().add(Component.translatable(item.getDescriptionId() + ".tooltip." + suffix, args));
    }

    /**
     * Create's a {@link LangBuilder} with a localized tooltip of the given {@link Block}. <br>
     * For a block with id "foo:bar", the key of the tooltip should be: "block.foo.bar.tooltip.suffix"
     * 
     * @param block  the Block
     * @param suffix a suffix for the key for different content
     * @param args   args for the tooltip {@link MutableComponent}
     * @return the LangBuilder
     */
    public LangBuilder tooltip(Block block, String suffix, Object... args) {
        return builder().add(Component.translatable(block.getDescriptionId() + ".tooltip." + suffix, args));
    }

    /**
     * Create's a {@link LangBuilder} with a localized component with its key generated from the category and id. <br>
     * For category "cat" and id "foo:bar", the key should be "cat.foo.bar"
     * 
     * @param category the category
     * @param loc      the id
     * @param args     args for the {@link MutableComponent}
     * @return the LangBuilder
     */
    public LangBuilder fromRL(String category, ResourceLocation loc, Object... args) {
        return builder().add(Component.translatable(Util.makeDescriptionId(category, loc), args));
    }

    /**
     * Create's a {@link LangBuilder} with a number in the correct format. <br>
     * 
     * @param d the number
     * @return the LangBuilder
     */
    public LangBuilder number(double d) {
        return builder().text(LangNumberFormat.format(d));
    }

    /**
     * Create's a {@link LangBuilder} with a localized component
     * 
     * @param langKey the translation key for the {@link MutableComponent}
     * @param args    args for the {@link MutableComponent}
     * @return the LangBuilder
     */
    public LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    /**
     * Create's a {@link LangBuilder} with a String
     * 
     * @param text the String
     * @return the LangBuilder
     */
    public LangBuilder text(String text) {
        return builder().text(text);
    }
}
