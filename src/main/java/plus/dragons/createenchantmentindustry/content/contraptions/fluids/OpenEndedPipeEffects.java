package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import com.simibubi.create.content.contraptions.fluids.OpenEndedPipe;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.ModFluids;

import java.util.List;
import java.util.Random;

public class OpenEndedPipeEffects {
    private static Random RNG = new Random();

    public static void register(){
        OpenEndedPipe.registerEffectHandler(new ExperienceEffect());
    }

    public static class ExperienceEffect implements OpenEndedPipe.IEffectHandler{

        @Override
        public boolean canApplyEffects(OpenEndedPipe pipe, FluidStack fluid) {
            return fluid.getFluid().isSame(ModFluids.EXPERIENCE.get());
        }

        @Override
        public void applyEffects(OpenEndedPipe pipe, FluidStack fluid) {
            List<Player> players = pipe.getWorld()
                    .getEntitiesOfClass(Player.class, pipe.getAOE(), LivingEntity::isAlive);
            if(players.isEmpty()){
                var level = pipe.getWorld();
                var pos = pipe.getOutputPos();
                var pipePos = pipe.getPos();
                var speed = new Vec3(pos.getX() - pipePos.getX(),pos.getY() - pipePos.getY(),pos.getZ() - pipePos.getZ()).scale(0.2);
                var endPos = new AABB(pos).getCenter();
                var expBall = new ExperienceOrb(level,endPos.x,endPos.y,endPos.z,fluid.getAmount());
                expBall.setDeltaMovement(speed);
                level.addFreshEntity(expBall);
            } else {
                var amount = fluid.getAmount()/players.size();
                var left = fluid.getAmount()%players.size();
                players.forEach(player -> player.giveExperiencePoints(amount));
                if(left!=0) players.get(RNG.nextInt(players.size())).giveExperiencePoints(left);
            }
        }
    }
}
