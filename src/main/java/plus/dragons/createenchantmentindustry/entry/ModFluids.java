package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class ModFluids {

    public static final ResourceLocation EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<VirtualFluid> EXPERIENCE =
            EnchantmentIndustry.registrate().virtualFluid("experience", EXPERIENCE_STILL_RL, EXPERIENCE_FLOW_RL)
                    .lang("Liquid Experience")
                    .properties(builder -> builder.lightLevel(15))
                    .register();

    public static final ResourceLocation HYPER_EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_still");
    public static final ResourceLocation HYPER_EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_flow");

    public static final FluidEntry<VirtualFluid> HYPER_EXPERIENCE =
            EnchantmentIndustry.registrate().virtualFluid("hyper_experience", HYPER_EXPERIENCE_STILL_RL, HYPER_EXPERIENCE_FLOW_RL)
                    .lang("Liquid Hyper Experience")
                    .properties(builder -> builder.lightLevel(15))
                    .register();
    
    public static final ResourceLocation INK_STILL_RL = EnchantmentIndustry.genRL("fluid/ink_still");
    public static final ResourceLocation INK_FLOW_RL = EnchantmentIndustry.genRL("fluid/ink_flow");
    
    public static final FluidEntry<ForgeFlowingFluid.Flowing> INK =
            EnchantmentIndustry.registrate().fluid("ink", INK_STILL_RL, INK_FLOW_RL, ModFluids.NoColorFluidAttributes::new)
                    .properties(b -> b.viscosity(1000)
                            .density(1000))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(4)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
                    .tag(ModTags.ModFluidTags.INK.tag)
                    .bucket()
                    .build()
                    .register();

    public static void register() {
    }
    
    public static void handleInkEffect(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.tickCount % 20 != 0) return;
        if (entity.isEyeInFluid(ModTags.ModFluidTags.INK.tag())) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
        }
    }
    
    /**
     * Removing alpha from tint prevents optifine from forcibly applying biome
     * colors to modded fluids (Makes translucent fluids disappear)
     */
    private static class NoColorFluidAttributes extends AllFluids.TintedFluidType {

        public NoColorFluidAttributes(Properties properties, ResourceLocation stillTexture,
                                      ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }

    }

}
