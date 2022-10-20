package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import com.simibubi.create.content.contraptions.fluids.OpenEndedPipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.CeiAdvancements;

import java.util.Map;

public class ExperienceEffectHandler implements OpenEndedPipe.IEffectHandler {
    
    @Override
    public boolean canApplyEffects(OpenEndedPipe pipe, FluidStack fluid) {
        return fluid.getFluid().isSame(CeiFluids.EXPERIENCE.get());
    }
    
    @Override
    public void applyEffects(OpenEndedPipe pipe, FluidStack fluid) {
        var players = pipe.getWorld().getEntitiesOfClass(Player.class, pipe.getAOE(), LivingEntity::isAlive);
        var level = pipe.getWorld();
        var pos = pipe.getOutputPos();
        var pipePos = pipe.getPos();
        var speed = new Vec3(pos.getX() - pipePos.getX(), pos.getY() - pipePos.getY(), pos.getZ() - pipePos.getZ()).scale(0.2);
        var xpPos = new AABB(pos).getCenter();
        if (players.isEmpty()) {
            awardExperienceOrDrop(null, level, xpPos, speed, fluid.getAmount());
        } else {
            var amount = fluid.getAmount() / players.size();
            var left = fluid.getAmount() % players.size();
            players.forEach(player -> {
                CeiAdvancements.A_SHOWER_EXPERIENCE.getTrigger().trigger((ServerPlayer) player);
                player.giveExperiencePoints(amount);
                awardExperienceOrDrop(player, level, xpPos, speed, amount);
            });
            if (left != 0) {
                var lucky = players.get(level.random.nextInt(players.size()));
                awardExperienceOrDrop(lucky, level, xpPos, speed, left);
            }
        }
    }
    
    private void awardExperienceOrDrop(@Nullable Player player, Level level, Vec3 pos, Vec3 speed, int amount) {
        while(amount > 0) {
            int i = ExperienceOrb.getExperienceValue(amount);
            amount -= i;
            var expBall = new ExperienceOrb(level, pos.x, pos.y, pos.z, i);
            if (player == null || MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, expBall))) {
                expBall.setDeltaMovement(speed);
                level.addFreshEntity(expBall);
            } else {
                int left = repairPlayerItems(player, expBall.value);
                if (left > 0) {
                    player.giveExperiencePoints(left);
                }
            }
        }
    }
    
    private int repairPlayerItems(Player player, int amount) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack itemstack = entry.getValue();
            int i = Math.min((int) (amount * itemstack.getXpRepairRatio()), itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - i);
            int j = amount - durabilityToXp(i);
            return j > 0 ? repairPlayerItems(player, j) : 0;
        } else {
            return amount;
        }
    }
    
    private int durabilityToXp(int durability) {
        return durability / 2;
    }
    
}
