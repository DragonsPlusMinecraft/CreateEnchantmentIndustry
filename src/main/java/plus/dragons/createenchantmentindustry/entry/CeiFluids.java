package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceFluid;

public class CeiFluids {
    
    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
            .creativeModeTab(() -> AllCreativeModeTabs.BASE_CREATIVE_TAB);

    public static final ResourceLocation EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<ExperienceFluid> EXPERIENCE = REGISTRATE.virtualFluid("experience",
                    EXPERIENCE_STILL_RL, EXPERIENCE_FLOW_RL, null, ExperienceFluid::new)
                .lang("Liquid Experience")
                .attributes(builder -> builder.luminosity(15))
                .tag(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag, CeiTags.FluidTag.PRINTER_INPUT.tag)
                .register();

    public static final ResourceLocation HYPER_EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_still");
    public static final ResourceLocation HYPER_EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_flow");

    public static final FluidEntry<HyperExperienceFluid> HYPER_EXPERIENCE = REGISTRATE.virtualFluid("hyper_experience",
            HYPER_EXPERIENCE_STILL_RL, HYPER_EXPERIENCE_FLOW_RL, null, HyperExperienceFluid::new)
            .lang("Liquid Hyper Experience")
            .attributes(builder -> builder.luminosity(15))
            .tag(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag, CeiTags.FluidTag.PRINTER_INPUT.tag)
            .register();

    public static final ResourceLocation INK_STILL_RL = EnchantmentIndustry.genRL("fluid/ink_still");
    public static final ResourceLocation INK_FLOW_RL = EnchantmentIndustry.genRL("fluid/ink_flow");

    public static final FluidEntry<ForgeFlowingFluid.Flowing> INK =
            REGISTRATE.fluid("ink", INK_STILL_RL, INK_FLOW_RL, CeiFluids.NoColorFluidAttributes::new)
                    .attributes(b -> b.viscosity(1000)
                            .density(1000))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(4)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
                    .tag(CeiTags.FluidTag.INK.tag)
                    .bucket()
                    .build()
                    .register();

    public static void register() {
    }

    public static void handleInkEffect(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.tickCount % 20 != 0) return;
        if (entity.isEyeInFluid(CeiTags.FluidTag.INK.tag())) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
        }
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
