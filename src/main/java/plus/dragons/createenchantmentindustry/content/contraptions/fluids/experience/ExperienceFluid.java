package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import org.jetbrains.annotations.Nullable;

public class ExperienceFluid extends VirtualFluid {
    
    protected final int xpRatio;
    
    public ExperienceFluid(int xpRatio, Properties properties) {
        super(properties);
        this.xpRatio = xpRatio;
    }
    
    public ExperienceFluid(Properties properties) {
        this(1, properties);
    }
    
    public ExperienceOrb convertToOrb(Level level, double x, double y, double z, int fluidAmount) {
        return new ExperienceOrb(level, x, y, z, fluidAmount);
    }
    
    public void drop(ServerLevel level, Vec3 pos, int fluidAmount) {
        while(fluidAmount > 0) {
            int orbSize = ExperienceOrb.getExperienceValue(fluidAmount);
            fluidAmount -= orbSize;
            if (!ExperienceOrb.tryMergeToExisting(level, pos, orbSize)) {
                level.addFreshEntity(this.convertToOrb(level, pos.x, pos.y, pos.z, orbSize));
            }
        }
    }
    
    public void awardOrDrop(@Nullable Player player, ServerLevel level, Vec3 pos, Vec3 speed, int amount) {
        var orb = this.convertToOrb(level, pos.x, pos.y, pos.z, amount);
        if (player == null || MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb))) {
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
