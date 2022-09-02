package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import plus.dragons.createenchantmentindustry.entry.ModPackets;


public class EnchantingGuideScreen extends AbstractSimiContainerScreen<EnchantingGuideMenu> {

    public EnchantingGuideScreen(EnchantingGuideMenu container, Inventory inv, Component title) {
        // TODO a lot more to change
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(menu.scrollInputLabel);
        addRenderableWidget(menu.scrollInput);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {

    }

    @Override
    public void removed() {
        super.removed();
        ModPackets.channel.sendToServer(new EnchantingGuideEditPacket(menu.index,menu.getSlot(36).getItem()));
    }
}
