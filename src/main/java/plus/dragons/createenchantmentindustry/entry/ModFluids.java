package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class ModFluids {

    public static final ResourceLocation FLUID_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation FLUID_FLOWING_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<VirtualFluid> EXPERIENCE =
            EnchantmentIndustry.registrate().virtualFluid("experience", FLUID_STILL_RL, FLUID_FLOWING_RL)
                    .attributes(builder -> builder.luminosity(15))
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> INK =
            EnchantmentIndustry.registrate().fluid("ink", EnchantmentIndustry.genRL("fluid/ink_still"),EnchantmentIndustry.genRL("fluid/ink_flow"),ModFluids.NoColorFluidAttributes::new)
                    .attributes(b -> b.viscosity(1000)
                            .density(1000))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(4)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
                    .bucket()
                    .build()
                    .register();

    public static void register() {
    }

    /**
     * Removing alpha from tint prevents optifine from forcibly applying biome
     * colors to modded fluids (Makes translucent fluids disappear)
     */
    private static class NoColorFluidAttributes extends FluidAttributes {

        protected NoColorFluidAttributes(Builder builder, Fluid fluid) {
            super(builder, fluid);
        }

        @Override
        public int getColor(BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }

    }
}
