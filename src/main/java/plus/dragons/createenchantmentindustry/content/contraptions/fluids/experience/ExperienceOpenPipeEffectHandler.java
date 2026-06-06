package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

import java.util.List;

public class ExperienceOpenPipeEffectHandler implements OpenPipeEffectHandler {

    public static void register() {
        ExperienceOpenPipeEffectHandler handler = new ExperienceOpenPipeEffectHandler();
        OpenPipeEffectHandler.REGISTRY.register(CeiFluids.EXPERIENCE.getSource(), handler);
        OpenPipeEffectHandler.REGISTRY.register(CeiFluids.HYPER_EXPERIENCE.getSource(), handler);
    }

    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (!(fluid.getFluid() instanceof ExperienceFluid expFluid))
            return;

        int amount = fluid.getAmount();
        if (amount <= 0)
            return;

        List<Player> players = level.getEntitiesOfClass(Player.class, area, LivingEntity::isAlive);
        Vec3 pos = area.getCenter();
        if (players.isEmpty()) {
            expFluid.drop(serverLevel, pos, amount);
            return;
        }

        int partial = amount / players.size();
        int left = amount % players.size();
        for (Player player : players) {
            CeiAdvancements.A_SHOWER_EXPERIENCE.getTrigger().trigger((ServerPlayer) player);
            awardOrDropRaw(expFluid, player, serverLevel, pos, partial);
        }
        if (left > 0) {
            Player lucky = players.get(level.random.nextInt(players.size()));
            awardOrDropRaw(expFluid, lucky, serverLevel, pos, left);
        }
    }

    private static void awardOrDropRaw(ExperienceFluid fluid, Player player, ServerLevel level, Vec3 pos, int amount) {
        if (amount <= 0)
            return;
        var orb = fluid.convertToOrb(level, pos.x, pos.y, pos.z, amount);
        int value = orb.value;
        if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb))) {
            orb.value = value;
            level.addFreshEntity(orb);
            return;
        }
        int left = RawExperienceUtil.repairPlayerItems(orb, player, value);
        if (left > 0) {
            RawExperienceUtil.addRawExperience(player, left);
            fluid.applyAdditionalEffects(player, left);
        }
    }
}
