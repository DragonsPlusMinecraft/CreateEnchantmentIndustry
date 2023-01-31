package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.MobCategory;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottle;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrb;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrbRenderer;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiEntityTypes {

    public static final EntityEntry<HyperExperienceOrb> HYPER_EXPERIENCE_ORB = REGISTRATE.entity("hyper_experience_orb",
        HyperExperienceOrb::new,
        () -> HyperExperienceOrbRenderer::new,
        MobCategory.MISC,
        6, 20, true, false,
        HyperExperienceOrb::build
    ).register();

    public static final EntityEntry<HyperExperienceBottle> HYPER_EXPERIENCE_BOTTLE = REGISTRATE.entity("hyper_experience_bottle",
        HyperExperienceBottle::new,
        () -> ThrownItemRenderer<HyperExperienceBottle>::new,
        MobCategory.MISC,
        4, 10, true, false,
        HyperExperienceBottle::build
    ).lang("Thrown Bottle O' Hyper Enchanting").register();

    public static void register() {
    }
}
