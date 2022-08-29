package plus.dragons.createenchantmentindustry.registry;

import com.simibubi.create.AllTags;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class ModFluids {

    public static final ResourceLocation FLUID_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation FLUID_FLOWING_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<ForgeFlowingFluid.Flowing> EXPERIENCE =
            ModRegistries.REGISTRATE.fluid("experience",FLUID_STILL_RL,FLUID_FLOWING_RL)
                    .lang("Experience")
                    .tag(AllTags.forgeFluidTag("chocolate"))
                    .attributes(b -> b.viscosity(300)
                            .density(1000)
                            .luminosity(15))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();

    static void register(){}
}
