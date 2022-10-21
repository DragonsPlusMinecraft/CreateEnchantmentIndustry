package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.contraptions.fluids.OpenEndedPipe;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.CeiAdvancements;

public abstract class ExperienceEffectHandler<T extends ExperienceOrb> implements OpenEndedPipe.IEffectHandler {
    public static final ExperienceEffectHandler<ExperienceOrb> EXPERIENCE = new ExperienceEffectHandler<>() {
        @Override
        public ExperienceOrb createExperienceOrb(Level level, double x, double y, double z, int value) {
            return new ExperienceOrb(level, x, y, z, value);
        }

        @Override
        public boolean canApplyEffects(OpenEndedPipe pipe, FluidStack fluid) {
            return CeiFluids.EXPERIENCE.is(fluid.getFluid());
        }
    };
    public static final ExperienceEffectHandler<HyperExperienceOrb> HYPER_EXPERIENCE = new ExperienceEffectHandler<>() {
        @Override
        public HyperExperienceOrb createExperienceOrb(Level level, double x, double y, double z, int value) {
            return new HyperExperienceOrb(level, x, y, z, value);
        }

        @Override
        public void applyPlayerEffects(HyperExperienceOrb orb, Player player, int amount) {
            orb.applyPlayerEffects(player, amount);
        }

        @Override
        public boolean canApplyEffects(OpenEndedPipe pipe, FluidStack fluid) {
            return CeiFluids.HYPER_EXPERIENCE.is(fluid.getFluid());
        }
    };

    public abstract T createExperienceOrb(Level level, double x, double y, double z, int value);

    public void applyPlayerEffects(T orb, Player player, int amount) {}
    
    @Override
    public void applyEffects(OpenEndedPipe pipe, FluidStack fluid) {
        var players = pipe.getWorld().getEntitiesOfClass(Player.class, pipe.getAOE(), LivingEntity::isAlive);
        var level = pipe.getWorld();
        var pos = pipe.getOutputPos();
        var pipePos = pipe.getPos();
        var speed = new Vec3(pos.getX() - pipePos.getX(), pos.getY() - pipePos.getY(), pos.getZ() - pipePos.getZ()).scale(0.2);
        var xpPos = VecHelper.getCenterOf(pos);
        int amount = fluid.getAmount();
        if (players.isEmpty()) {
            awardExperienceOrDrop(null, level, xpPos, speed, amount);
        } else {
            int partial = amount / players.size();
            int left = amount % players.size();
            players.forEach(player -> {
                CeiAdvancements.A_SHOWER_EXPERIENCE.getTrigger().trigger((ServerPlayer) player);
                player.giveExperiencePoints(partial);
                awardExperienceOrDrop(player, level, xpPos, speed, partial);
            });
            if (left != 0) {
                var lucky = players.get(level.random.nextInt(players.size()));
                awardExperienceOrDrop(lucky, level, xpPos, speed, left);
            }
        }
    }
    
    protected void awardExperienceOrDrop(@Nullable Player player, Level level, Vec3 pos, Vec3 speed, int amount) {
        while(amount > 0) {
            int i = ExperienceOrb.getExperienceValue(amount);
            amount -= i;
            var orb = createExperienceOrb(level, pos.x, pos.y, pos.z, i);
            if (player == null || MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb))) {
                orb.setDeltaMovement(speed);
                level.addFreshEntity(orb);
            } else {
                int left = orb.repairPlayerItems(player, orb.value);
                if (left > 0) {
                    player.giveExperiencePoints(left);
                    applyPlayerEffects(orb, player, left);
                }
            }
        }
    }

}
