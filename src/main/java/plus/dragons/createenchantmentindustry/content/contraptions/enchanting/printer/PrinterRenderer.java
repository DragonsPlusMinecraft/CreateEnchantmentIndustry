package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiPartialModels;

public class PrinterRenderer extends SmartBlockEntityRenderer<PrinterBlockEntity> {
    public PrinterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    
    private static final PartialModel[] TUBE = { CeiPartialModels.PRINTER_TOP, CeiPartialModels.PRINTER_MIDDLE};
    
    @Override
    protected void renderSafe(PrinterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        SmartFluidTankBehaviour tank = be.tank;
        if (tank == null)
            return;
        //Render fluid
        SmartFluidTankBehaviour.TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        float level = primaryTank.getFluidLevel()
            .getValue(partialTicks);
    
        if (!fluidStack.isEmpty() && level != 0) {
            level = Math.max(level, 0.175f);
            float min = 2.5f / 16f;
            float max = min + (11 / 16f);
            float yOffset = (11 / 16f) * level;
            ms.pushPose();
            ms.translate(0, yOffset, 0);
            FluidRenderer.renderFluidBox(fluidStack, min, min - yOffset, min, max, min, max, buffer, ms, light,
                false);
            ms.popPose();
        }
        //Render partials
        int processingTicks = be.processingTicks;
        float processingPT = processingTicks - partialTicks;
    
        float squeeze = 0;
        if (processingPT < 0) {
            squeeze = 0;
        } else if (processingPT <= 10) {
            squeeze = Mth.lerp(processingPT / 10f, 0, -1);
        } else if (processingPT <= 90) {
            squeeze = -1;
        } else if (processingPT <= 100) {
            squeeze = Mth.lerp((100 - processingPT) / 10f, 0, -1);
        }
        
        ms.pushPose();
        for (PartialModel bit : TUBE) {
            ms.translate(0, -3 * squeeze / 32f, 0);
            CachedBufferer
                .partial(bit, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
        ms.popPose();
        
        BlockState blockState = be.getBlockState();
        CachedBufferer
            .partial(CeiPartialModels.PRINTER_BOTTOM, blockState)
            .translate(0, squeeze / 2f, 0)
            .light(light)
            .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
    
    
}
