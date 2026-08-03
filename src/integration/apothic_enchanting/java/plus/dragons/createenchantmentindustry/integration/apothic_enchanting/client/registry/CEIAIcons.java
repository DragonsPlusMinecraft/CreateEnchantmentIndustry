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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.registry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import plus.dragons.createdragonsplus.util.CodeReference;
import plus.dragons.createenchantmentindustry.common.CEICommon;

@CodeReference(value = AllIcons.class, source = "create", license = "mit")
public class CEIAIcons extends AllIcons {
    public static final CEIAIcons I_ETERNA = new CEIAIcons(0, 0),
            I_QUANTA = new CEIAIcons(1, 0),
            I_ARCANA = new CEIAIcons(2, 0);

    private static final ResourceLocation ATLAS = CEICommon.asResource("textures/gui/icons.png");

    protected final int iconX;
    protected final int iconY;

    public CEIAIcons(int x, int y) {
        super(x, y);
        iconX = x * 16;
        iconY = y * 16;
    }

    protected ResourceLocation getAtlas() {
        return ATLAS;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, getAtlas());
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack ms, MultiBufferSource buffer, int color) {
        VertexConsumer builder = buffer.getBuffer(RenderType.text(getAtlas()));
        Matrix4f matrix = ms.last().pose();
        Color rgb = new Color(color);
        int light = LightTexture.FULL_BRIGHT;

        Vec3 vec1 = new Vec3(0, 0, 0);
        Vec3 vec2 = new Vec3(0, 1, 0);
        Vec3 vec3 = new Vec3(1, 1, 0);
        Vec3 vec4 = new Vec3(1, 0, 0);

        float u1 = iconX * 1f / ICON_ATLAS_SIZE;
        float u2 = (iconX + 16) * 1f / ICON_ATLAS_SIZE;
        float v1 = iconY * 1f / ICON_ATLAS_SIZE;
        float v2 = (iconY + 16) * 1f / ICON_ATLAS_SIZE;

        vertex(builder, matrix, vec1, rgb, u1, v1, light);
        vertex(builder, matrix, vec2, rgb, u1, v2, light);
        vertex(builder, matrix, vec3, rgb, u2, v2, light);
        vertex(builder, matrix, vec4, rgb, u2, v1, light);
    }

    @OnlyIn(Dist.CLIENT)
    private void vertex(VertexConsumer builder, Matrix4f matrix, Vec3 vec, Color rgb, float u, float v, int light) {
        builder.vertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z)
                .color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255)
                .uv(u, v)
                .uv2(light)
                .endVertex();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(getAtlas(), x, y, 0, iconX, iconY, 16, 16, ICON_ATLAS_SIZE, ICON_ATLAS_SIZE);
    }
}
