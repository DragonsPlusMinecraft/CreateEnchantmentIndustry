package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
    @Accessor("count")
    int create_enchantment_industry$getCount();

    @Accessor("count")
    void create_enchantment_industry$setCount(int count);

    @Invoker("repairPlayerItems")
    int create_enchantment_industry$repairPlayerItems(Player player, int value);
}
