package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceFluid;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.fluid.FluidLavaReaction;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.fluid.NoTintFluidType;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiFluids {

    public static final ResourceLocation EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<ExperienceFluid> EXPERIENCE = REGISTRATE.virtualFluid("experience",
            EXPERIENCE_STILL_RL, EXPERIENCE_FLOW_RL, CreateRegistrate::defaultFluidType, ExperienceFluid::createSource, ExperienceFluid::createFlowing)
            .lang("Liquid Experience")
            .properties(builder -> builder.lightLevel(15))
            .tag(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag, CeiTags.FluidTag.PRINTER_INPUT.tag)
            .register();

    public static final ResourceLocation HYPER_EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_still");
    public static final ResourceLocation HYPER_EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_flow");

    public static final FluidEntry<HyperExperienceFluid> HYPER_EXPERIENCE = REGISTRATE.virtualFluid("hyper_experience",
            HYPER_EXPERIENCE_STILL_RL, HYPER_EXPERIENCE_FLOW_RL, CreateRegistrate::defaultFluidType, HyperExperienceFluid::createSource, HyperExperienceFluid::createFlowing)
            .lang("Liquid Hyper Experience")
            .properties(builder -> builder.lightLevel(15))
            .tag(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag, CeiTags.FluidTag.PRINTER_INPUT.tag)
            .register();

    public static final ResourceLocation INK_STILL_RL = EnchantmentIndustry.genRL("fluid/ink_still");
    public static final ResourceLocation INK_FLOW_RL = EnchantmentIndustry.genRL("fluid/ink_flow");

    public static final FluidEntry<BaseFlowingFluid.Flowing> INK = REGISTRATE
            .fluid("ink", INK_STILL_RL, INK_FLOW_RL, NoTintFluidType::new)
            .properties(b -> b.viscosity(1000)
                    .density(1000))
            .fluidProperties(p -> p.levelDecreasePerBlock(2)
                    .tickRate(25)
                    .slopeFindDistance(4)
                    .explosionResistance(100f))
            .source(BaseFlowingFluid.Source::new)
            .tag(CeiTags.FluidTag.INK.tag)
            .bucket()
            .build()
            .register();

    public static void register() {
    }

    public static void handleInkEffect(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity.tickCount % 20 != 0) return;
            if (entity.isEyeInFluidType(INK.getType())) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
            }
        }
    }

    public static void registerLavaReaction() {
        FluidLavaReaction.register(INK.getType(),
            Blocks.OBSIDIAN.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState()
        );
    }

    public static boolean isExperienceFluid(FluidStack stack) {
        return stack.getFluid().isSame(EXPERIENCE.get()) || stack.getFluid().isSame(HYPER_EXPERIENCE.get());
    }

}
