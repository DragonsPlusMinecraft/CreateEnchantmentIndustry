package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.MobCategory;
import plus.dragons.createdragonlib.entry.RegistrateHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottle;

public class CeiEntityTypes {

    public static final EntityEntry<HyperExperienceBottle> HYPER_EXP_BOTTLE = RegistrateHelper.EntityType.register(EnchantmentIndustry.registrate(), "hyper_experience_bottle", HyperExperienceBottle::new, () -> ThrownItemRenderer<HyperExperienceBottle>::new,
            MobCategory.MISC, 4, 10, true, false, HyperExperienceBottle::build).register();

    public static void register() {
    }
}
