package plus.dragons.createenchantmentindustry.dragonLibLegacy.lang;

import java.util.List;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

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
 * Utility class for building {@link MutableComponent}. <br>
 * Originated from: {@link //com.simibubi.create.foundation.utility.LangBuilder}
 */
public class LangBuilder {
    String modid;
    MutableComponent component;

    public LangBuilder(String modid) {
        this.modid = modid;
    }

    public LangBuilder space() {
        return text(" ");
    }

    public LangBuilder newLine() {
        return text("\n");
    }

    /**
     * Appends a localised component<br>
     * To add an independently formatted localised component, use add() and a nested
     * builder
     *
     * @param langKey
     * @param args
     * @return
     */
    public LangBuilder translate(String langKey, Object... args) {
        return add(Component.translatable(modid + "." + langKey, net.createmod.catnip.lang.LangBuilder.resolveBuilders(args)));
    }

    /**
     * Appends a text component
     *
     * @param literalText
     * @return
     */
    public LangBuilder text(String literalText) {
        return add(Component.literal(literalText));
    }

    /**
     * Appends a colored text component
     *
     * @param format
     * @param literalText
     * @return
     */
    public LangBuilder text(ChatFormatting format, String literalText) {
        return add(Component.literal(literalText).withStyle(format));
    }

    /**
     * Appends a colored text component
     *
     * @param color
     * @param literalText
     * @return
     */
    public LangBuilder text(int color, String literalText) {
        return add(Component.literal(literalText).withStyle(s -> s.withColor(color)));
    }

    /**
     * Appends the contents of another builder
     *
     * @param otherBuilder
     * @return
     */
    public LangBuilder add(LangBuilder otherBuilder) {
        return add(otherBuilder.component());
    }

    /**
     * Appends a component
     *
     * @param customComponent
     * @return
     */
    public LangBuilder add(MutableComponent customComponent) {
        component = component == null ? customComponent : component.append(customComponent);
        return this;
    }

    /**
     * Applies the format to all added components
     *
     * @param format
     * @return
     */
    public LangBuilder style(ChatFormatting format) {
        assertComponent();
        component = component.withStyle(format);
        return this;
    }

    /**
     * Applies the color to all added components
     *
     * @param color
     * @return
     */
    public LangBuilder color(int color) {
        assertComponent();
        component = component.withStyle(s -> s.withColor(color));
        return this;
    }

    public MutableComponent component() {
        assertComponent();
        return component;
    }

    public String string() {
        return component().getString();
    }

    public String json() {
        return Component.Serializer.toJson(component());
    }

    public void sendStatus(Player player) {
        player.displayClientMessage(component(), true);
    }

    public void sendChat(Player player) {
        player.displayClientMessage(component(), false);
    }

    public void addTo(List<? super MutableComponent> tooltip) {
        tooltip.add(component());
    }

    public void forGoggles(List<? super MutableComponent> tooltip) {
        forGoggles(tooltip, 0);
    }

    public void forGoggles(List<? super MutableComponent> tooltip, int indents) {
        tooltip.add(new LangBuilder(modid)
                .text(Strings.repeat(' ', 4 + indents))
                .add(this)
                .component());
    }

    private void assertComponent() {
        if (component == null)
            throw new IllegalStateException("No components were added to builder");
    }
}
