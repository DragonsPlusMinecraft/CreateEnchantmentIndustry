package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
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
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class EnchantingAlterRenderer extends SmartTileEntityRenderer<EnchantingAlterBlockEntity> {
    public static final Material BOOK_MATERIAL = ForgeHooksClient.getBlockMaterial(EnchantmentIndustry.genRL("block/enchanting_alter_book"));
    private static final float PI = 3.14159265358979323846f;
    private final BookModel bookModel;
    
    public EnchantingAlterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }
    
    @Override
    protected void renderSafe(EnchantingAlterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ps, buffer, light, overlay);
        float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(partialTicks));
        float animation = be.headAnimation.getValue(partialTicks) * .175f;
        
        ps.pushPose();
    
        renderItem(be, partialTicks, ps, buffer, light, overlay);
        renderBlaze(be, be.getBlockState(), horizontalAngle, animation, ps, buffer);
        renderBook(be, horizontalAngle, partialTicks, ps, buffer, light, overlay);
        
        ps.popPose();
    }
    
    protected void renderItem(EnchantingAlterBlockEntity be, float partialTicks,
                              PoseStack ps, MultiBufferSource buffer,
                              int light, int overlay) {
        TransportedItemStack transported = be.heldItem;
        if(transported == null)
            return;
        
        Direction insertedFrom = transported.insertedFrom;
        boolean horizontal = insertedFrom.getAxis().isHorizontal();
        
        ps.pushPose();
        
        float beltOffset = horizontal ? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition) : .5f;
        float processingProgress = be.processingTicks <= 0
            ? 0
            : (EnchantingAlterBlockEntity.ENCHANTING_TIME - be.processingTicks + partialTicks) / 40;
        float movingProgress = Mth.sin((1 - 2 * Mth.abs(.5f - beltOffset)) * PI / 2) ;
        float verticalOffset = movingProgress * (5 + Mth.sin(processingProgress * 2 * PI)) / 8;
        ps.translate(.5f, 13 / 16f + verticalOffset, .5f);
        
        Vec3 offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal()).scale(.5f - beltOffset);
        ps.translate(offsetVec.x, 0, offsetVec.z);
        
        if(horizontal) {
            float sideOffset = Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
            sideOffset = Mth.lerp(movingProgress, sideOffset, 0);
            boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
            ps.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : -sideOffset);
        }
        
        float rotProgress = processingProgress - Mth.floor(processingProgress);
        float rotX = rotProgress * 2 * PI + PI / 2;
        float rotY = rotProgress * 2 * PI + PI + transported.angle * movingProgress;
        float rotZ = rotProgress * 2 * PI;
        ps.mulPose(Quaternion.fromXYZ(rotX, rotY, rotZ));
        ps.scale(0.5f, 0.5f, 0.5f);
    
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(transported.stack, ItemTransforms.TransformType.FIXED, light, overlay, ps, buffer, 0);
        
        ps.popPose();
    }
    
    protected void renderBlaze(EnchantingAlterBlockEntity be, BlockState blockState,
                               float horizontalAngle, float animation,
                               PoseStack ps, MultiBufferSource buffer) {
        
        boolean working = be.processingTicks > 0;
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float renderTick = time + (be.hashCode() % 13) * 16f;
        float offsetMult = 64;
        float offset = Mth.sin((renderTick / 16f) % (2 * PI)) / offsetMult;
        float offset1 = Mth.sin((renderTick / 16f + PI) % (2 * PI)) / offsetMult;
        float offset2 = Mth.sin((renderTick / 16f + PI / 2) % (2 * PI)) / offsetMult;
    
        VertexConsumer solid = buffer.getBuffer(RenderType.solid());
        
        ps.pushPose();
        ps.translate(0, 3 / 8f, 0);
        
        PartialModel blazeModel = working ? AllBlockPartials.BLAZE_ACTIVE : AllBlockPartials.BLAZE_IDLE;
        
        float headY = offset - (animation * .75f);
        //Head
        draw(CachedBufferer
            .partial(blazeModel, blockState)
            .translate(0, headY, 0),
            horizontalAngle, ps, solid
        );
        //Rods
        PartialModel rods = AllBlockPartials.BLAZE_BURNER_RODS;
        PartialModel rods2 =AllBlockPartials.BLAZE_BURNER_RODS_2;
        draw(CachedBufferer
            .partial(rods, blockState)
            .translate(0, offset1 + animation - 1 / 8f, 0),
            0, ps, solid
        );
        draw(CachedBufferer
            .partial(rods2, blockState)
            .translate(0, offset2 + animation - 7 / 16f, 0),
            0, ps, solid
        );
        
        ps.popPose();
    }
    
    protected void renderBook(EnchantingAlterBlockEntity be,
                              float horizontalAngle, float partialTicks,
                              PoseStack ps, MultiBufferSource buffer,
                              int light, int overlay) {
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
        this.bookModel.render(ps, vertexconsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
        
        ps.popPose();
    }
    
    private void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ps, VertexConsumer vc) {
        buffer.rotateCentered(Direction.UP, horizontalAngle)
            .light(LightTexture.FULL_BRIGHT)
            .renderInto(ps, vc);
    }
}
