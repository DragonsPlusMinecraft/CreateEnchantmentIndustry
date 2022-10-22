package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import mezz.jei.api.gui.drawable.IDrawable;
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
    public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset + 20, 100);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f));
        GuiGameElement.of(CeiBlocks.DISENCHANTER.getDefaultState())
                .lighting(DEFAULT_LIGHTING)
                .scale(20)
                .render(matrixStack);
        matrixStack.popPose();
    }
}
