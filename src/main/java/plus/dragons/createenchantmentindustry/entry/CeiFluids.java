package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import plus.dragons.createdragonlib.api.event.FluidLavaInteractionRegisterEvent;
import plus.dragons.createdragonlib.entry.RegistrateHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiFluids {
    
    
    private static final CreateRegistrate REGISTRATE = EnchantmentIndustry.registrate()
        .creativeModeTab(() -> Create.BASE_CREATIVE_TAB).startSection(AllSections.MATERIALS);

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
            EnchantmentIndustry.registrate().fluid("ink", INK_STILL_RL, INK_FLOW_RL, RegistrateHelper.Fluid.NoColorFluidAttributes::new)
                    .properties(b -> b.viscosity(1000)
                            .density(1000))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(4)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
                    .tag(CeiTags.FluidTags.INK.tag())
                    .bucket()
                    .build()
                    .register();

    public static void register() {
    }

    public static void handleInkEffect(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.tickCount % 20 != 0) return;
        if (entity.isEyeInFluid(CeiTags.FluidTags.INK.tag())) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
        }
    }

    public static void handleInkLavaReaction(FluidLavaInteractionRegisterEvent event) {
        event.register((fluidState) -> fluidState.is(CeiTags.FluidTags.INK.tag()), Blocks.BLACKSTONE.defaultBlockState());
    }


}
