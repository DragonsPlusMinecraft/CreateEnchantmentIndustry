package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HyperExperienceOrbRenderer extends EntityRenderer<HyperExperienceOrb> {
    private static final ResourceLocation HYPER_EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(HYPER_EXPERIENCE_ORB_LOCATION);

    public HyperExperienceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    protected int getBlockLightLevel(HyperExperienceOrb orb, BlockPos pos) {
        return 15;
    }

    public void render(HyperExperienceOrb orb, float yaw, float partialTicks,
                       PoseStack ps, MultiBufferSource buffer, int light) {
        ps.pushPose();
        int i = orb.getIcon();
        float u1 = (i % 4 * 16) / 64.0F;
        float u2 = (i % 4 * 16 + 16) / 64.0F;
        float v1 = (i / 4 * 16) / 64.0F;
        float v2 = (i / 4 * 16 + 16) / 64.0F;
        float time = (orb.tickCount + partialTicks) / 2.0F;
        int red = (int) (((Mth.sin(time + 4.1887903F) + 1.0F) * 0.1F + 0.1F) * 255.0F);
        int green = (int) (((Mth.sin(time) + 1.0F) * 0.5F + 0.5F) * 255.0F);
        ps.translate(0, .1, 0);
        ps.mulPose(this.entityRenderDispatcher.cameraOrientation());
        ps.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        ps.scale(.3F, .3F, .3F);
        VertexConsumer vertexconsumer = buffer.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = ps.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, -0.25F, red, green, 255, u1, v2, light);
        vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, -0.25F, red, green, 255, u2, v2, light);
        vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, 0.75F, red, green, 255, u2, v1, light);
        vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, 0.75F, red, green, 255, u1, v1, light);
        ps.popPose();
        super.render(orb, yaw, partialTicks, ps, buffer, light);
    }

    private void vertex(VertexConsumer buffer, Matrix4f pose, Matrix3f normal,
                        float x, float y,
                        int r, int g, int b,
                        float u, float v, int light) {
        buffer.vertex(pose, x, y, 0.0F)
                .color(r, g, b, 128)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(HyperExperienceOrb pEntity) {
        return HYPER_EXPERIENCE_ORB_LOCATION;
    }

}
