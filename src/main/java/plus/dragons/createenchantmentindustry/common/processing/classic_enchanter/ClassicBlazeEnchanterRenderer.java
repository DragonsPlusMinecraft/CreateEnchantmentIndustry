/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class ClassicBlazeEnchanterRenderer extends BlazeBlockRenderer<ClassicBlazeEnchanterBlockEntity> {
    public static final Material BOOK_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, CEICommon.asResource("block/blaze_enchanter_book"));
    private static final float PI = 3.14159265358979323846f;
    private final BookModel bookModel;
    private final ItemRenderer itemRenderer;

    public ClassicBlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected void renderSafe(ClassicBlazeEnchanterBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        ClassicEnchanterBehaviourIndicatorRenderer.renderOnBlockEntity(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        var item = blockEntity.heldItem;
        float horizontalAngle = AngleHelper.rad(blockEntity.headAngle().getValue(partialTicks));
        renderBook(blockEntity, partialTicks, horizontalAngle, poseStack, bufferSource);
        if (!item.isEmpty())
            renderItem(blockEntity, item, blockEntity.processingTime, partialTicks, poseStack, bufferSource, light, overlay);
        if (VisualizationManager.supportsVisualization(blockEntity.getLevel()))
            return;
        poseStack.pushPose();
        poseStack.translate(0, 0.2, 0);
        super.renderSafe(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        poseStack.popPose();
    }

    protected void renderItem(ClassicBlazeEnchanterBlockEntity blockEntity, ItemStack item, int processingTime, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        Level level = blockEntity.getLevel();
        assert level != null;
        var blockPos = blockEntity.getBlockPos();
        float renderTicks = AnimationTickHolder.getTicks(level);
        float animation = processingTime == -1
                ? 0
                : Mth.sin((processingTime + partialTicks) / 20f);
        float height = 1.25f + (1 + animation) * .25f;
        float xRot = (renderTicks * 5 + blockPos.getX()) % 360;
        float zRot = (renderTicks * 5 + blockPos.getZ()) % 360;
        poseStack.pushPose();
        poseStack.translate(.5f, height, .5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
        poseStack.scale(.5f, .5f, .5f);
        itemRenderer.renderStatic(item, ItemDisplayContext.FIXED, light, overlay, poseStack, bufferSource, level, blockEntity.hashCode());
        poseStack.popPose();
    }

    protected void renderBook(ClassicBlazeEnchanterBlockEntity be,
            float partialTicks, float horizontalAngle,
            PoseStack ps, MultiBufferSource buffer) {
        ps.pushPose();
        ps.translate(0.5, 0.25, 0.5);
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        ps.translate(0.0, 0.1f + Mth.sin(time * 0.1f) * 0.01, 0.0);
        ps.mulPose(Axis.YP.rotation(horizontalAngle + PI / 2));
        ps.mulPose(Axis.ZP.rotationDegrees(80.0f));
        ps.scale(1.2f, 1.2f, 1.2f);
        float flip = Mth.lerp(partialTicks, be.oFlip, be.flip);
        float page0 = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
        float page1 = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;
        this.bookModel.setupAnim(time, Mth.clamp(page0, 0.0f, 1.0f), Mth.clamp(page1, 0.0f, 1.0f), 1);
        VertexConsumer vertexconsumer = BOOK_MATERIAL.buffer(buffer, RenderType::entitySolid);
        this.bookModel.render(
                ps, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        ps.popPose();
    }
}
