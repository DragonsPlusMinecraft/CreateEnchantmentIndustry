package plus.dragons.createenchantmentindustry.foundation.gui;

import com.google.common.collect.AbstractIterator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An extension for {@link Label} which renders {@link Component} correctly.
 */
public class ComponentLabel extends Label {
    
    public ComponentLabel(int x, int y, Component text) {
        super(x, y, text);
    }
    
    private Iterator<Component> getComponentIterator(Component root) {
        return new AbstractIterator<>() {
            private final Deque<Component> stack = new LinkedList<>(Collections.singleton(root));
            
            @Nullable
            @Override
            protected Component computeNext() {
                if(stack.isEmpty()) {
                    return endOfData();
                } else {
                    Component ret = stack.pop();
                    List<Component> siblings = new ArrayList<>(ret.getSiblings());
                    Collections.reverse(siblings);
                    for(Component c : siblings) {
                        stack.push(c);
                    }
                    return ret;
                }
            }
        };
    }
    
    private MutableComponent computeTrimmedText(Component text, boolean trimFront, int maxWidthPx) {
        maxWidthPx -= font.width("...");
        int totalWidthPx = 0;
        Iterator<Component> texts = getComponentIterator(text);
        List<Component> result = new ArrayList<>();
        collect:
        while(texts.hasNext()) {
            //Add components to list
            Component component = texts.next();
            // TODO fix this
            // String content = component.getContents();
            String content = component.getString();
            int widthPx = font.width(Components.literal(content).setStyle(text.getStyle()));
            if(totalWidthPx < maxWidthPx) {
                result.add(component);
                totalWidthPx += widthPx;
                continue;
            }
            //Split tail component
            int stringLength = content.length();
            if (stringLength == 0) continue;
            int startIndex = trimFront ? 0 : stringLength - 1;
            int endIndex = !trimFront ? 0 : stringLength - 1;
            int step;
            if(startIndex > endIndex) {
                step = -1;
            } else if(startIndex < endIndex) {
                step = 1;
            } else {
                result.add(Components.literal(content).setStyle(component.getStyle()));
                break;
            }
            StringBuilder builder = new StringBuilder(content);
            for (int i = startIndex; i != endIndex; i += step) {
                String sub = builder.substring(trimFront ? i : startIndex, trimFront ? endIndex + 1 : i + 1);
                if (font.width(Components.literal(sub).setStyle(text.getStyle())) <= maxWidthPx) {
                    result.add(Components.literal(sub).setStyle(text.getStyle()));
                    break collect;
                }
            }
        }
        //Compute result component
        if(trimFront) {
            var trim = Components.literal("...").setStyle(result.get(0).getStyle());
            result.forEach(trim::append);
            return trim;
        } else {
            var trim = Components.literal("...").setStyle(result.get(result.size() - 1).getStyle());
            var ret = Components.literal("");
            result.forEach(ret::append);
            return ret.append(trim);
        }
    }
    
    @Override
    public void setTextAndTrim(Component newText, boolean trimFront, int maxWidthPx) {
        if(suffix != null) maxWidthPx -= font.width(suffix);
        text = font.width(newText) <= maxWidthPx
            ? newText
            : computeTrimmedText(newText, trimFront, maxWidthPx);
    }
    
    @Override
    public void renderButton(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (text == null)
            return;
        RenderSystem.setShaderColor(1, 1, 1, 1);
        var textToRender = suffix == null
            ? text
            : text.copy().append(suffix);
        if (hasShadow)
            font.drawShadow(matrixStack, textToRender, x, y, color);
        else
            font.draw(matrixStack, textToRender, x, y, color);
    }
    
}
