package plus.dragons.createenchantmentindustry.foundation.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.FurnaceExpExtractor;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract public class AbstractFurnaceBlockEntityMixin<T> extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Final
    @Shadow
    private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

    @Unique
    LazyOptional<IFluidHandler> createEnchantmentIndustry$expExtractor = LazyOptional.of(this::createEnchantmentIndustry$createExpExtractor);

    @Unique
    private IFluidHandler createEnchantmentIndustry$createExpExtractor(){
        return new FurnaceExpExtractor(recipesUsed,(AbstractFurnaceBlockEntity)(Object)this);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return createEnchantmentIndustry$expExtractor.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        createEnchantmentIndustry$expExtractor.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.createEnchantmentIndustry$expExtractor = LazyOptional.of(this::createEnchantmentIndustry$createExpExtractor);
    }
}
