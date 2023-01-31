package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottle;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrb;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrbRenderer;

public class CeiEntityTypes {
    
    public static final EntityEntry<HyperExperienceOrb> HYPER_EXPERIENCE_ORB = builder("hyper_experience_orb",
        HyperExperienceOrb::new,
        () -> HyperExperienceOrbRenderer::new,
        MobCategory.MISC, 6, 20, true, false, HyperExperienceOrb::build
    ).register();

    public static final EntityEntry<HyperExperienceBottle> HYPER_EXPERIENCE_BOTTLE = builder("hyper_experience_bottle",
        HyperExperienceBottle::new,
        () -> ThrownItemRenderer<HyperExperienceBottle>::new,
        MobCategory.MISC, 4, 10, true, false, HyperExperienceBottle::build
    ).lang("Thrown Bottle O' Hyper Enchanting").register();

    private static <T extends Entity> CreateEntityBuilder<T, ?> builder(String name, EntityType.EntityFactory<T> factory,
                                                                        NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
                                                                        MobCategory group, int range, int updateFrequency, boolean sendVelocity, boolean immuneToFire,
                                                                        NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
        String id = Lang.asId(name);
        return (CreateEntityBuilder<T, ?>) EnchantmentIndustry.registrate()
                .entity(id, factory, group)
                .properties(b -> b.setTrackingRange(range)
                        .setUpdateInterval(updateFrequency)
                        .setShouldReceiveVelocityUpdates(sendVelocity))
                .properties(propertyBuilder)
                .properties(b -> {
                    if (immuneToFire)
                        b.fireImmune();
                })
                .renderer(renderer);
    }

    public static void register() {}
    
}
