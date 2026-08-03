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

import static plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterRenderer.BOOK_MATERIAL;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ClassicBlazeEnchanterItemRenderer extends CustomRenderedItemModelRenderer {
    private final Supplier<BookModel> bookModelSupplier;
    private BookModel bookModel;

    public ClassicBlazeEnchanterItemRenderer() {
        this.bookModelSupplier = () -> new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);
        poseStack.pushPose();
        poseStack.translate(0, -0.3, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.translate(0, 0.05, 0);
        poseStack.scale(1.2f, 1.2f, 1.2f);
        VertexConsumer vertexconsumer = BOOK_MATERIAL.buffer(bufferSource, RenderType::entitySolid);
        if (bookModel == null) {
            bookModel = bookModelSupplier.get();
            float page0 = Mth.frac(0.25f) * 1.6f - 0.3f;
            float page1 = Mth.frac(0.75f) * 1.6f - 0.3f;
            bookModel.setupAnim(0, Mth.clamp(page0, 0.0f, 1.0f), Mth.clamp(page1, 0.0f, 1.0f), 1);
        }
        this.bookModel.render(
                poseStack, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
    }
}
