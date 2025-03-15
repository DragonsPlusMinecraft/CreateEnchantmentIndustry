package plus.dragons.createenchantmentindustry.dragonLibLegacy.init;

import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;

public class SafeRegistrate extends CreateRegistrate {
    
    public SafeRegistrate(String modid) {
        super(modid);
    }
    
    @Override
    public SafeRegistrate registerEventListeners(IEventBus bus) {
        super.registerEventListeners(bus);
        return this;
    }
    
    public <T extends Entity> CreateEntityBuilder<T, CreateRegistrate> entity(
        String name,
        EntityType.EntityFactory<T> factory,
        NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
        MobCategory group,
        int range, int updateFrequency,
        boolean sendVelocity, boolean immuneToFire,
        NonNullConsumer<EntityType.Builder<T>> propertyBuilder)
    {
        String id = Lang.asId(name);
        var builder = this.entity(id, factory, group);
        builder.properties(b -> {
                if (immuneToFire)
                    b.fireImmune();
                b.setTrackingRange(range)
                    .setUpdateInterval(updateFrequency)
                    .setShouldReceiveVelocityUpdates(sendVelocity);
                propertyBuilder.accept(b);
            })
            .renderer(renderer);
        return builder;
    }
    
}
