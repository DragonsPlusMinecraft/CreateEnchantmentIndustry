package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import plus.dragons.createdragonlib.gui.ComponentLabel;
import plus.dragons.createenchantmentindustry.entry.CeiPackets;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static plus.dragons.createenchantmentindustry.foundation.gui.CeiGuiTextures.ENCHANTING_GUIDE;

public class EnchantingGuideScreen extends AbstractSimiContainerScreen<EnchantingGuideMenu> {
    private static final int ENCHANTING_GUIDE_WIDTH = 178;
    private List<Rect2i> extraAreas = Collections.emptyList();
    public int index;
    public SelectionScrollInput scrollInput;
    public Label scrollInputLabel;
    private boolean directItemStackEdit;
    @Nullable
    private BlockPos blockPos;

    public EnchantingGuideScreen(EnchantingGuideMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        directItemStackEdit = container.directItemStackEdit;
        blockPos = container.blockPos;
    }

    public void updateScrollInput(boolean resetIndex) {
        if (resetIndex) {
            index = 0;
        }

        scrollInput.forOptions(menu.enchantments);
        scrollInput.setState(index);
    }

    @Override
    protected void init() {
        setWindowSize(
                ENCHANTING_GUIDE.width,
                ENCHANTING_GUIDE.height + 4 + PLAYER_INVENTORY.height
        );
        setWindowOffset(-32, 0);
        super.init();
        int guideX = getLeftOfCentered(ENCHANTING_GUIDE_WIDTH);
        int guideY = topPos;
        extraAreas = ImmutableList.of(
                new Rect2i(guideX + ENCHANTING_GUIDE.width, guideY + ENCHANTING_GUIDE.height - 48, 48, 48),
                new Rect2i(guideX, guideY, imageWidth, imageHeight)
        );
        index = menu.contentHolder.getOrCreateTag().getInt("index");
        scrollInput = new SelectionScrollInput(guideX + 40, guideY + 22, 120, 16);
        scrollInputLabel = new ComponentLabel(guideX + 43, guideY + 26, Components.immutableEmpty()).withShadow();
        scrollInput.calling(index -> this.index = index).writingTo(scrollInputLabel);
        addRenderableWidget(scrollInputLabel);
        addRenderableWidget(scrollInput);
        updateScrollInput(false);
    }

    @Override
    protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
        int invY = topPos + ENCHANTING_GUIDE.height + 4;
        renderPlayerInventory(ms, invX, invY);

        int guideX = getLeftOfCentered(ENCHANTING_GUIDE_WIDTH);
        int guideY = topPos;

        ENCHANTING_GUIDE.render(ms, guideX, guideY, this);
        drawCenteredString(ms, font, title, guideX + ENCHANTING_GUIDE_WIDTH / 2, guideY + 3, 0xFFFFFF);

        GuiGameElement.of(menu.contentHolder)
                .<GuiGameElement.GuiRenderBuilder>at(
                        guideX + ENCHANTING_GUIDE.width,
                        guideY + ENCHANTING_GUIDE.height - 48,
                        -200
                )
                .scale(3)
                .render(ms);
    }

    @Override
    public void removed() {
        super.removed();
        if(directItemStackEdit)
            CeiPackets.channel.sendToServer(new EnchantingGuideEditPacket(index, menu.getSlot(36).getItem()));
        else
            CeiPackets.channel.sendToServer(new BlazeEnchanterEditPacket(index, menu.getSlot(36).getItem(), blockPos));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
