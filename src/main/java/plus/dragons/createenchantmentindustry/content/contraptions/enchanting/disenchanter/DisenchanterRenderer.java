package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.ShadowRenderHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class DisenchanterRenderer extends SmartTileEntityRenderer<DisenchanterBlockEntity> {
    public DisenchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(DisenchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
                              int light, int overlay) {
        super.renderSafe(be, partialTicks, ps, buffer, light, overlay);
        renderItem(be, partialTicks, ps, buffer, light, overlay);
        renderFluid(be, partialTicks, ps, buffer, light);
    }

    protected void renderItem(DisenchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
                              int light, int overlay) {
        TransportedItemStack transported = be.heldItem;
        if (transported == null) return;

        TransformStack ts = TransformStack.cast(ps);

        Direction insertedFrom = transported.insertedFrom;
        boolean horizontal = insertedFrom.getAxis().isHorizontal();

        ps.pushPose();
        ps.translate(.5f, 13 / 16f, .5f);
        ts.nudge(0);
        float offset = horizontal ? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition) : .5f;
        float sideOffset = horizontal ? Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset) : .5f;

        Vec3 offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal()).scale(.5f - offset);
        ps.translate(offsetVec.x, 0, offsetVec.z);
        if (horizontal) {
            boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
            ps.translate(alongX ? sideOffset : 0, 0.005f, alongX ? 0 : -sideOffset);
        } else {
            ps.translate(0, 0.005f, 0);
        }

        ShadowRenderHelper.renderShadow(ps, buffer, .75f, .2f);

        ItemStack itemStack = transported.stack;
        Random r = new Random(0);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        int count = Mth.log2(itemStack.getCount()) / 2;
        boolean blockItem = itemRenderer.getModel(itemStack, null, null, 0).isGui3d();

        if (blockItem) {
            ps.translate(0, 1 / 8f, 0);
        } else {
            ps.translate(0, 1 / 64f, 0);
        }

        int positive = insertedFrom.getAxisDirection().getStep();
        float verticalAngle = positive * offset * 360 + 180;
        if (insertedFrom.getAxis() != Direction.Axis.X)
            ts.rotateX(verticalAngle);
        if (insertedFrom.getAxis() != Direction.Axis.Z)
            ts.rotateZ(-verticalAngle);

        int processingTicks = be.processingTicks;
        float processingProgress = switch (processingTicks) {
            case 0, DisenchanterBlockEntity.DISENCHANTER_TIME -> 0;
            default -> Mth.clamp((processingTicks - partialTicks) / DisenchanterBlockEntity.DISENCHANTER_TIME, 0, 1);
        };
        ts.rotateY(processingProgress * 360);

        for (int i = 0; i <= count; i++) {
            ps.pushPose();
            if (blockItem) {
                ps.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
            }
            ps.scale(.5f, .5f, .5f);
            if (!blockItem) ts.rotateX(90);
            itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, light, overlay, ps, buffer, 0);
            ps.popPose();

            if (!blockItem) ts.rotateY(10);
            ps.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
        }

        ps.popPose();
    }

    protected void renderFluid(DisenchanterBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer,
                               int light) {
        SmartFluidTankBehaviour tank = be.internalTank;
        if (tank == null)
            return;

        SmartFluidTankBehaviour.TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack tankFluidStack = primaryTank.getRenderedFluid();
        float level = primaryTank.getFluidLevel().getValue(partialTicks);

        if (!tankFluidStack.isEmpty() && level != 0) {
            float yMin = 5f / 16f;
            float min = 2f / 16f;
            float max = min + (12 / 16f);
            float yOffset = (7 / 16f) * level;
            ps.pushPose();
            ps.translate(0, yOffset, 0);
            FluidRenderer.renderFluidBox(tankFluidStack, min, yMin - yOffset, min, max, yMin, max, buffer, ps, light,
                    false);
            ps.popPose();
        }

        if (be.processingTicks == 0) return;

        TransportedItemStack transported = be.heldItem;
        if (transported == null) return;

        Direction insertedFrom = transported.insertedFrom;
        boolean horizontal = insertedFrom.getAxis().isHorizontal();

        var result = Disenchanting.disenchantResult(transported.stack, be.getLevel());
        if (result == null) return;
        FluidStack xp = result.getFirst();

        int processingTicks = be.processingTicks;
        float processingProgress = processingTicks == 0 ? 0
                : Mth.clamp(1 - (processingTicks - partialTicks - 5) / 10, 0, 1);
        float radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
        Vec3 itemPosition = new Vec3(0.5, 0, 0.5);
        float offset = horizontal ? Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition) : .5f;
        float sideOffset = horizontal ? Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset) : .5f;
        itemPosition = itemPosition.add(Vec3.atLowerCornerOf(insertedFrom.getOpposite().getNormal()).scale(.5f - offset));
        if (horizontal) {
            boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
            itemPosition.add(alongX ? sideOffset : 0, 0.005, alongX ? 0 : -sideOffset);
        } else {
            itemPosition.add(0, 0.005, 0);
        }
        AABB bb = new AABB(itemPosition.add(0, 13 / 16d, 0), itemPosition.add(0, 1 / 4d, 0)).inflate(radius / 32f);
        FluidRenderer.renderFluidBox(xp,
                (float) bb.minX, (float) bb.minY, (float) bb.minZ,
                (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ,
                buffer, ps, light, true
        );
    }
}
