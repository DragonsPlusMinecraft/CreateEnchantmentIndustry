package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class HyperExperienceFluid extends ExperienceFluid {
    
    public HyperExperienceFluid(Properties properties) {
        super(10, properties);
    }
    
    @Override
    public HyperExperienceOrb convertToOrb(Level level, double x, double y, double z, int fluidAmount) {
        return new HyperExperienceOrb(level, x, y, z, fluidAmount * xpRatio);
    }
    
    @Override
    public void applyAdditionalEffects(LivingEntity entity, int expAmount) {
        int duration = 200 * Mth.ceillog2(expAmount);
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration));
    }
    
}
