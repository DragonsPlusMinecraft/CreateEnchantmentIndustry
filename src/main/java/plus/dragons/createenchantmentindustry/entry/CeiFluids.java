package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import plus.dragons.createdragonlib.fluid.FluidLavaReaction;
import plus.dragons.createdragonlib.fluid.NoTintFluidType;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiFluids {
    
    static {
        REGISTRATE.creativeModeTab(() -> Create.BASE_CREATIVE_TAB).startSection(AllSections.MATERIALS);
    }

    public static final ResourceLocation EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<VirtualFluid> EXPERIENCE = REGISTRATE
        .virtualFluid("experience", EXPERIENCE_STILL_RL, EXPERIENCE_FLOW_RL)
        .lang("Liquid Experience")
        .properties(builder -> builder.lightLevel(15))
        .register();

    public static final ResourceLocation HYPER_EXPERIENCE_STILL_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_still");
    public static final ResourceLocation HYPER_EXPERIENCE_FLOW_RL = EnchantmentIndustry.genRL("fluid/hyper_experience_flow");

    public static final FluidEntry<VirtualFluid> HYPER_EXPERIENCE = REGISTRATE
            .virtualFluid("hyper_experience", HYPER_EXPERIENCE_STILL_RL, HYPER_EXPERIENCE_FLOW_RL)
            .lang("Liquid Hyper Experience")
            .properties(builder -> builder.lightLevel(15))
            .register();

    public static final ResourceLocation INK_STILL_RL = EnchantmentIndustry.genRL("fluid/ink_still");
    public static final ResourceLocation INK_FLOW_RL = EnchantmentIndustry.genRL("fluid/ink_flow");

    public static final FluidEntry<ForgeFlowingFluid.Flowing> INK = REGISTRATE
            .fluid("ink", INK_STILL_RL, INK_FLOW_RL, NoTintFluidType::new)
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
        if (entity.isEyeInFluidType(INK.getType())) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, false, false));
        }
    }

    public static void registerLavaReaction() {
        FluidLavaReaction.register(INK.getType(),
            Blocks.OBSIDIAN.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState()
        );
    }

}
