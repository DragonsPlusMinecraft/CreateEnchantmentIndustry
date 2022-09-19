package plus.dragons.createenchantmentindustry.content.contraptions.enchantments;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;

public class EnchantingAlterRenderer extends SmartTileEntityRenderer<EnchantingAlterBlockEntity> {
    public static final Material BOOK_LOCATION = ForgeHooksClient.getBlockMaterial(new ResourceLocation("entity/enchanting_table_book"));
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
        
        renderBook(be, partialTicks, ps, buffer, light, overlay);
        renderBlaze(be, buffer, ps, be.getBlockState(), horizontalAngle, animation);
        
        ps.popPose();
    }
    
    protected void renderBlaze(EnchantingAlterBlockEntity be, MultiBufferSource buffer,
                             PoseStack ps, BlockState blockState, float horizontalAngle, float animation) {
        
        boolean working = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float renderTick = time + (be.hashCode() % 13) * 16f;
        float offsetMult = 64;
        float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
        float offset1 = Mth.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
        float offset2 = Mth.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
    
        VertexConsumer solid = buffer.getBuffer(RenderType.solid());
        VertexConsumer cutout = buffer.getBuffer(RenderType.cutoutMipped());
        
        ps.pushPose();
        ps.translate(0, 0.25, 0);
        
        if (working) {
            SpriteShiftEntry spriteShift = AllSpriteShifts.BURNER_FLAME;
            
            float spriteWidth = spriteShift.getTarget()
                .getU1()
                - spriteShift.getTarget()
                .getU0();
            
            float spriteHeight = spriteShift.getTarget()
                .getV1()
                - spriteShift.getTarget()
                .getV0();
            
            float speed = 5 / 64f;
            
            double vScroll = speed * time;
            vScroll = vScroll - Math.floor(vScroll);
            vScroll = vScroll * spriteHeight / 2;
            
            double uScroll = speed * time / 2;
            uScroll = uScroll - Math.floor(uScroll);
            uScroll = uScroll * spriteWidth / 2;
            
            draw(CachedBufferer
                .partial(AllBlockPartials.BLAZE_BURNER_FLAME, blockState)
                .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll), horizontalAngle, ps, cutout
            );
        }
        
        PartialModel blazeModel = working ? AllBlockPartials.BLAZE_ACTIVE : AllBlockPartials.BLAZE_IDLE;
        
        float headY = offset - (animation * .75f);
        //Head
        draw(CachedBufferer
            .partial(blazeModel, blockState)
            .translate(0, headY, 0), horizontalAngle, ps, solid
        );
        //Goggles
        draw(CachedBufferer
            .partial(AllBlockPartials.BLAZE_GOGGLES, blockState)
            .translate(0, headY + 8 / 16f, 0), horizontalAngle, ps, solid
        );
        //Rods
        PartialModel rods = AllBlockPartials.BLAZE_BURNER_RODS;
        PartialModel rods2 =AllBlockPartials.BLAZE_BURNER_RODS_2;
        draw(CachedBufferer
            .partial(rods, blockState)
            .translate(0, offset1 + animation + .125f, 0), 0, ps, solid
        );
        draw(CachedBufferer
            .partial(rods2, blockState)
            .translate(0, offset2 + animation - 3 / 16f, 0), 0, ps, solid
        );
        
        ps.popPose();
    }
    
    protected void renderBook(EnchantingAlterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
        ps.pushPose();
        ps.translate(0.5, 0.25, 0.5);
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        ps.translate(0.0, 0.1f + Mth.sin(time * 0.1f) * 0.01, 0.0);
        
        float rotDiff = be.rot - be.oRot;
        while(rotDiff >= Math.PI) {
            rotDiff -= ((float)Math.PI * 2f);
        }
        while(rotDiff < -Math.PI) {
            rotDiff += ((float)Math.PI * 2f);
        }
        float rot = be.oRot + rotDiff * partialTicks;
        ps.mulPose(Vector3f.YP.rotation(-rot));
        ps.mulPose(Vector3f.ZP.rotationDegrees(80.0f));
        float flip = Mth.lerp(partialTicks, be.oFlip, be.flip);
        float page0 = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
        float page1 = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;
        float open = Mth.lerp(partialTicks, be.oOpen, be.open);
        
        this.bookModel.setupAnim(time, Mth.clamp(page0, 0.0f, 1.0f), Mth.clamp(page1, 0.0f, 1.0f), open);
        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(buffer, RenderType::entitySolid);
        this.bookModel.render(ps, vertexconsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
        ps.popPose();
    }
    
    private void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ps, VertexConsumer vc) {
        buffer.rotateCentered(Direction.UP, horizontalAngle)
            .light(LightTexture.FULL_BRIGHT)
            .renderInto(ps, vc);
    }
}
