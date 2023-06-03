package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock.HeatLevel;

import static plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity.ENCHANTING_TIME;

public class BlazeEnchanterRenderer extends SmartBlockEntityRenderer<BlazeEnchanterBlockEntity> {
    public static final Material BOOK_MATERIAL = ForgeHooksClient.getBlockMaterial(EnchantmentIndustry.genRL("block/blaze_enchanter_book"));
    private static final float PI = 3.14159265358979323846f;
    private final BookModel bookModel;
    
    public BlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }
    
    @Override
    protected void renderSafe(BlazeEnchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ps, buffer, light, overlay);
        float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(partialTicks));
        float animation = be.headAnimation.getValue(partialTicks) * .175f;
        
        ps.pushPose();
    
        renderItem(be, partialTicks, animation, ps, buffer);
        renderBlaze(be, horizontalAngle, animation, ps, buffer);
        renderBook(be, partialTicks, horizontalAngle, ps, buffer);
        
        ps.popPose();
    }
    
    protected void renderItem(BlazeEnchanterBlockEntity be,
                              float partialTicks, float animation,
                              PoseStack ps, MultiBufferSource buffer) {
        TransportedItemStack transported = be.heldItem;
        if(transported == null)
            return;
        
        Direction insertedFrom = transported.insertedFrom;
        boolean horizontal = insertedFrom.getAxis().isHorizontal();
        
        ps.pushPose();
    
        HeatLevel heatLevel = be.getBlockState().getValue(BlazeEnchanterBlock.HEAT_LEVEL);
        boolean active = be.processingTicks > 0 && be.processingTicks < 200;
        float renderTick = AnimationTickHolder.getRenderTime(be.getLevel()) + (be.hashCode() % 13) * 16f;
        float beltOffset = horizontal ? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition) : .5f;
        float movingProgress = Mth.sin((1 - 2 * Mth.abs(.5f - beltOffset)) * PI / 2);
        float verticalOffsetMult = heatLevel.isAtLeast(HeatLevel.KINDLED) ? 64 : 16;
        float verticalOffset = movingProgress * 5 / 8
            + Mth.sin((renderTick / 16f) % (2 * PI)) / verticalOffsetMult
            + animation * .75f;
        ps.translate(.5f, 3 / 4f + verticalOffset, .5f);
        
        Vec3 offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal()).scale(.5f - beltOffset);
        ps.translate(offsetVec.x, 0, offsetVec.z);
        
        if(horizontal) {
            float sideOffset = Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
            sideOffset = Mth.lerp(movingProgress, sideOffset, 0);
            boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
            ps.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : -sideOffset);
        }
        
        float rot = active
            ? (ENCHANTING_TIME - be.processingTicks + partialTicks) / 16 % (2 * PI)
            : renderTick / 16f % (2 * PI);
        float rotX = active ? rot + PI / 2 : 0;
        float rotY = rot + PI + transported.angle * movingProgress;
        float rotZ = active ? rot : 0;
        ps.mulPose(Quaternion.fromXYZ(rotX, rotY, rotZ));
        
        ps.scale(0.5f, 0.5f, 0.5f);
    
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(transported.stack, ItemTransforms.TransformType.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ps, buffer, 0);
        
        ps.popPose();
    }
    
    protected void renderBlaze(BlazeEnchanterBlockEntity be,
                               float horizontalAngle, float animation,
                               PoseStack ps, MultiBufferSource buffer) {
        BlockState blockState = be.getBlockState();
        HeatLevel heatLevel = blockState.getValue(BlazeEnchanterBlock.HEAT_LEVEL);
        boolean smouldering = heatLevel == HeatLevel.SMOULDERING;
        boolean active = be.processingTicks > 0 && be.processingTicks < 200;
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float renderTick = time + (be.hashCode() % 13) * 16f;
        float offsetMult = heatLevel.isAtLeast(HeatLevel.KINDLED) ? 64 : 16;
        float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
        float offset1 = Mth.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
        float offset2 = Mth.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
        float headY = offset + (animation * .75f);
        VertexConsumer solid = buffer.getBuffer(RenderType.solid());
        
        ps.pushPose();
        ps.translate(0, .125, 0);
        
        PartialModel blazeModel = switch(heatLevel) {
            case SEETHING -> active ? AllPartialModels.BLAZE_SUPER_ACTIVE : AllPartialModels.BLAZE_SUPER;
            case KINDLED -> active ? AllPartialModels.BLAZE_ACTIVE : AllPartialModels.BLAZE_IDLE;
            default -> AllPartialModels.BLAZE_INERT;
        };
        
        SuperByteBuffer blazeBuffer = CachedBufferer.partial(blazeModel, blockState);
        blazeBuffer.translate(0, headY, 0);
        draw(blazeBuffer, horizontalAngle, ps, solid);
        
        if (be.goggles) {
            PartialModel gogglesModel = blazeModel == AllPartialModels.BLAZE_INERT
                ? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;
            
            SuperByteBuffer gogglesBuffer = CachedBufferer.partial(gogglesModel, blockState);
            gogglesBuffer.translate(0, headY + 8 / 16f, 0);
            draw(gogglesBuffer, horizontalAngle, ps, solid);
        }
        
        if (!smouldering) {
            PartialModel rodsModel = heatLevel == HeatLevel.SEETHING
                ? AllPartialModels.BLAZE_BURNER_SUPER_RODS
                : AllPartialModels.BLAZE_BURNER_RODS;
            PartialModel rodsModel2 = heatLevel == HeatLevel.SEETHING
                ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2
                : AllPartialModels.BLAZE_BURNER_RODS_2;
            SuperByteBuffer rodsBuffer = CachedBufferer.partial(rodsModel, blockState);
            rodsBuffer.translate(0, offset1 + animation + .125f, 0)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ps, solid);
        
            SuperByteBuffer rodsBuffer2 = CachedBufferer.partial(rodsModel2, blockState);
            rodsBuffer2.translate(0, offset2 + animation - 3 / 16f, 0)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ps, solid);
        }
        
        ps.popPose();
    }
    
    protected void renderBook(BlazeEnchanterBlockEntity be,
                              float partialTicks, float horizontalAngle,
                              PoseStack ps, MultiBufferSource buffer) {
        ps.pushPose();
        
        ps.translate(0.5, 0.25, 0.5);
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        ps.translate(0.0, 0.1f + Mth.sin(time * 0.1f) * 0.01, 0.0);
        ps.mulPose(Vector3f.YP.rotation(horizontalAngle + PI / 2));
        ps.mulPose(Vector3f.ZP.rotationDegrees(80.0f));
        float flip = Mth.lerp(partialTicks, be.oFlip, be.flip);
        float page0 = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
        float page1 = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;
        this.bookModel.setupAnim(time, Mth.clamp(page0, 0.0f, 1.0f), Mth.clamp(page1, 0.0f, 1.0f), 1);
        VertexConsumer vertexconsumer = BOOK_MATERIAL.buffer(buffer, RenderType::entitySolid);
        this.bookModel.render(ps, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        
        ps.popPose();
    }
    
    private void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ps, VertexConsumer vc) {
        buffer.rotateCentered(Direction.UP, horizontalAngle)
            .light(LightTexture.FULL_BRIGHT)
            .renderInto(ps, vc);
    }
    
}
