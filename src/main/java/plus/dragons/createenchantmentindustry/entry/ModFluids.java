package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class ModFluids {

    public static final ResourceLocation FLUID_STILL_RL = EnchantmentIndustry.genRL("fluid/experience_still");
    public static final ResourceLocation FLUID_FLOWING_RL = EnchantmentIndustry.genRL("fluid/experience_flow");

    public static final FluidEntry<VirtualFluid> EXPERIENCE =
            EnchantmentIndustry.registrate().virtualFluid("experience")
                    .lang("Liquid Experience")
                    .register();

    public static void register(){}
}
