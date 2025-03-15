package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.fluids.VirtualFluid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import javax.annotation.Nullable;

public class ExperienceFluid extends VirtualFluid {
    
    protected final int xpRatio;
    
    public ExperienceFluid(int xpRatio, Properties properties) {
        super(properties,true);
        this.xpRatio = xpRatio;
    }
    
    public ExperienceFluid(Properties properties) {
        this(1, properties);
    }
    
    public ExperienceOrb convertToOrb(Level level, double x, double y, double z, int fluidAmount) {
        return new ExperienceOrb(level, x, y, z, fluidAmount);
    }
    
    public void drop(ServerLevel level, Vec3 pos, int fluidAmount) {
        ExperienceOrb.award(level, pos, fluidAmount);
    }
    
    public void awardOrDrop(@Nullable ServerPlayer player, ServerLevel level, Vec3 pos, Vec3 speed, int amount) {
        var orb = this.convertToOrb(level, pos.x, pos.y, pos.z, amount);
        if (player == null || NeoForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb)).isCanceled()) {
            if (!ExperienceOrb.tryMergeToExisting(level, pos, orb.value)) {
                orb.setDeltaMovement(speed);
                level.addFreshEntity(orb);
            }
        } else {
            int left = orb.repairPlayerItems(player, orb.value);
            if (left > 0) {
                player.giveExperiencePoints(left);
                this.applyAdditionalEffects(player, left);
            }
        }
    }
    
    public void applyAdditionalEffects(LivingEntity entity, int expAmount) {
    
    }
    
    public int getXpRatio() {
        return xpRatio;
    }
    
}
