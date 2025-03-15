package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SimpleTrigger extends AbstractTrigger<SimpleTrigger.Instance> {

    public SimpleTrigger(ResourceLocation id) {
        super(id);
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, null);
    }

    public Instance instance() {
        return new SimpleTrigger.Instance();
    }

    @Override
    public Codec<SimpleTrigger.Instance> codec() {
        return SimpleTrigger.Instance.CODEC;
    }

    public static class Instance extends AbstractTrigger.Instance {
        private static final Codec<SimpleTrigger.Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SimpleTrigger.Instance::player)
        ).apply(instance, SimpleTrigger.Instance::new));

        private final Optional<ContextAwarePredicate> player;

        public Instance() {
            player = Optional.empty();
        }

        public Instance(Optional<ContextAwarePredicate> player) {
            this.player = player;
        }

        @Override
        protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
            return true;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
    
}
