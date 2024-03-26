package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

public class ExperienceEffectHandler implements OpenEndedPipe.IEffectHandler {
    
    @Override
    public boolean canApplyEffects(OpenEndedPipe pipe, FluidStack fluid) {
        return fluid.getFluid() instanceof ExperienceFluid;
    }
    
    @Override
    public void applyEffects(OpenEndedPipe pipe, FluidStack fluidStack) {
        if (pipe.getWorld() instanceof PonderWorld){
            var level = pipe.getWorld();
            var pos = pipe.getOutputPos();
            var pipePos = pipe.getPos();
            var speed = new Vec3(pos.getX() - pipePos.getX() + Math.random() * 0.1,
                    pos.getY() - pipePos.getY() + Math.random() * 0.1,
                    pos.getZ() - pipePos.getZ() + Math.random() * 0.1).scale(0.2);
            var orbPos = VecHelper.getCenterOf(pos);
            var orb = new ExperienceOrb(level, orbPos.x, orbPos.y, orbPos.z, 1);
            orb.setDeltaMovement(speed);
            level.addFreshEntity(orb);
            return;
        }
        if (!(pipe.getWorld() instanceof ServerLevel level))
            return;
        var players = level.getEntitiesOfClass(Player.class, pipe.getAOE(), LivingEntity::isAlive);
        var pos = pipe.getOutputPos();
        var pipePos = pipe.getPos();
        var speed = new Vec3(pos.getX() - pipePos.getX(),
                             pos.getY() - pipePos.getY(),
                             pos.getZ() - pipePos.getZ()).scale(0.2);
        var orbPos = VecHelper.getCenterOf(pos);
        ExperienceFluid fluid = (ExperienceFluid) fluidStack.getFluid();
        int amount = fluidStack.getAmount();
        if (players.isEmpty()) {
            fluid.awardOrDrop(null, level, orbPos, speed, amount);
        } else {
            int partial = amount / players.size();
            int left = amount % players.size();
            players.forEach(player -> {
                CeiAdvancements.A_SHOWER_EXPERIENCE.getTrigger().trigger((ServerPlayer) player);
                fluid.awardOrDrop(player, level, orbPos, speed, partial);
            });
            if (left != 0) {
                var lucky = players.get(level.random.nextInt(players.size()));
                fluid.awardOrDrop(lucky, level, orbPos, speed, left);
            }
        }
    }
    
}
