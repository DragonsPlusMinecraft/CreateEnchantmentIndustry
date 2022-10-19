package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import com.simibubi.create.content.contraptions.fluids.OpenEndedPipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.CeiAdvancements;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class OpenEndedPipeEffects {
    private static final Random RNG = new Random();

    public static void register() {
        OpenEndedPipe.registerEffectHandler(new ExperienceEffect());
    }

    public static class ExperienceEffect implements OpenEndedPipe.IEffectHandler {

        @Override
        public boolean canApplyEffects(OpenEndedPipe pipe, FluidStack fluid) {
            return fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get());
        }

        @Override
        public void applyEffects(OpenEndedPipe pipe, FluidStack fluid) {
            List<Player> players = pipe.getWorld()
                    .getEntitiesOfClass(Player.class, pipe.getAOE(), LivingEntity::isAlive);
            if (players.isEmpty()) {
                var level = pipe.getWorld();
                var pos = pipe.getOutputPos();
                var pipePos = pipe.getPos();
                var speed = new Vec3(pos.getX() - pipePos.getX(), pos.getY() - pipePos.getY(), pos.getZ() - pipePos.getZ()).scale(0.2);
                var endPos = new AABB(pos).getCenter();
                var expBall = new ExperienceOrb(level, endPos.x, endPos.y, endPos.z, fluid.getAmount());
                expBall.setDeltaMovement(speed);
                level.addFreshEntity(expBall);
            } else {
                var amount = fluid.getAmount() / players.size();
                var left = fluid.getAmount() % players.size();
                players.forEach(player -> {
                    CeiAdvancements.A_SHOWER_EXPERIENCE.getTrigger().trigger((ServerPlayer) player);
                    player.giveExperiencePoints(amount);
                    var expBall = new ExperienceOrb(player.level,player.getX(),player.getY(),player.getZ(),amount);
                    if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, expBall))) return;
                    int i = repairPlayerItems(player, expBall.value);
                    if (i > 0) {
                        player.giveExperiencePoints(i);
                    }

                });
                if (left != 0) {
                    var lucky = players.get(RNG.nextInt(players.size()));
                    var expBall = new ExperienceOrb(lucky.level,lucky.getX(),lucky.getY(),lucky.getZ(),left);
                    if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(lucky, expBall))) return;
                    int i = repairPlayerItems(lucky, expBall.value);
                    if (i > 0) {
                        lucky.giveExperiencePoints(i);
                    }
                }
            }
        }
    }

    private static int repairPlayerItems(Player pPlayer, int pRepairAmount) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, pPlayer, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack itemstack = entry.getValue();
            int i = Math.min((int) (pRepairAmount * itemstack.getXpRepairRatio()), itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - i);
            int j = pRepairAmount - durabilityToXp(i);
            return j > 0 ? repairPlayerItems(pPlayer, j) : 0;
        } else {
            return pRepairAmount;
        }
    }

    private static int durabilityToXp(int pDurability) {
        return pDurability / 2;
    }
}
