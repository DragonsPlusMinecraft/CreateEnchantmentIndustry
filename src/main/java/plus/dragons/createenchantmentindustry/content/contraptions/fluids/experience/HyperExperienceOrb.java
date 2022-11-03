package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.network.NetworkHooks;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

public class HyperExperienceOrb extends ExperienceOrb {
    
    public HyperExperienceOrb(Level level, double x, double y, double z, int value) {
        this(CeiEntityTypes.HYPER_EXPERIENCE_ORB.get(), level);
        this.setPos(x, y, z);
        this.setYRot((float)(this.random.nextDouble() * 360.0D));
        this.setDeltaMovement((this.random.nextDouble() * (double)0.2F - (double)0.1F) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * (double)0.2F - (double)0.1F) * 2.0D);
        this.value = value;
    }
    
    public HyperExperienceOrb(EntityType<? extends HyperExperienceOrb> entityType, Level level) {
        super(entityType, level);
    }
    
    @SuppressWarnings("unchecked")
    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<HyperExperienceOrb> entityBuilder = (EntityType.Builder<HyperExperienceOrb>) builder;
        return entityBuilder.sized(.5f, .5f);
    }
    
    public void applyPlayerEffects(Player player, int expAmount) {
        CeiFluids.HYPER_EXPERIENCE.get().applyAdditionalEffects(player, expAmount);
    }
    
    @Override
    public void playerTouch(Player player) {
        if (!this.level.isClientSide) {
            if (player.takeXpDelay == 0) {
                if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, this)))
                    return;
                player.takeXpDelay = 2;
                player.take(this, 1);
                int i = this.repairPlayerItems(player, this.value);
                if (i > 0) {
                    player.giveExperiencePoints(i);
                    applyPlayerEffects(player, i);
                }
                
                --this.count;
                if (this.count == 0) {
                    this.discard();
                }
            }
        }
    }
    
    public int getIcon() {
        int value = this.value / 10;
        if (value >= 2477) {
            return 10;
        } else if (value >= 1237) {
            return 9;
        } else if (value >= 617) {
            return 8;
        } else if (value >= 307) {
            return 7;
        } else if (value >= 149) {
            return 6;
        } else if (value >= 73) {
            return 5;
        } else if (value >= 37) {
            return 4;
        } else if (value >= 17) {
            return 3;
        } else if (value >= 7) {
            return 2;
        } else {
            return value >= 3 ? 1 : 0;
        }
    }
    
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
}
