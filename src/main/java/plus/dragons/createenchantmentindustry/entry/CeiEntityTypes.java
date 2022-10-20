package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.MobCategory;
import plus.dragons.createdragonlib.entry.RegistrateHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.HyperExpBottle;

public class CeiEntityTypes {

    public static final EntityEntry<HyperExpBottle> HYPER_EXP_BOTTLE = RegistrateHelper.EntityType.register(EnchantmentIndustry.registrate(), "hyper_experience_bottle", HyperExpBottle::new, () -> ThrownItemRenderer<HyperExpBottle>::new,
            MobCategory.MISC, 4, 10, true, false, HyperExpBottle::build).register();

    public static void register() {
    }
}
