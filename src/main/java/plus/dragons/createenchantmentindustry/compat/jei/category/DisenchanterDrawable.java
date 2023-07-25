package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.math.Axis;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

public class DisenchanterDrawable implements IDrawable {

    public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
            .firstLightRotation(12.5f, 45.0f)
            .secondLightRotation(-20.0f, 50.0f)
            .build();


    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        var matrixStack = guiGraphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset + 20, 100);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        GuiGameElement.of(CeiBlocks.DISENCHANTER.getDefaultState())
                .lighting(DEFAULT_LIGHTING)
                .scale(20)
                .render(guiGraphics);
        matrixStack.popPose();
    }
}
