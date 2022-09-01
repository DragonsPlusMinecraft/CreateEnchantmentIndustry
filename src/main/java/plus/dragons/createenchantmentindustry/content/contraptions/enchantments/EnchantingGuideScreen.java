package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnchantingGuideScreen extends AbstractSimiContainerScreen<EnchantingGuideMenu> {
    public EnchantingGuideScreen(EnchantingGuideMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {

    }
}
