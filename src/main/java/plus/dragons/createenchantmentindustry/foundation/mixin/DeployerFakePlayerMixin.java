package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import java.util.Arrays;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.MendingByDeployer;
@Mixin(value = DeployerFakePlayer.class, remap = false)
public class DeployerFakePlayerMixin {

    @Inject(method = "deployerKillsDoNotSpawnXP", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingExperienceDropEvent;setCanceled(Z)V"))
    private static void deployerKillsSpawnXpNuggets(LivingExperienceDropEvent event, CallbackInfo ci) {

        DeployerFakePlayer player = (DeployerFakePlayer) event.getAttackingPlayer();
        assert player != null;
        if (player.getRandom().nextFloat() > CeiConfigs.SERVER.deployerXpDropChance.getF())
            return;


        ItemStack deployerTool = player.getInventory().getItem(0);
        int xp = event.getDroppedExperience();

        if(MendingByDeployer.canItemBeMended(deployerTool)) {
                player.getInventory().setItem(0, MendingByDeployer.mendItem(xp, deployerTool));
                xp = MendingByDeployer.getNewXp(xp, deployerTool);
                event.setDroppedExperience(xp);
        }

        int amount = xp / 3 + (player.getRandom().nextInt(3) < xp % 3 ? 1 : 0);
        if (amount <= 0) return;
        Item nugget = AllItems.EXP_NUGGET.get();
        int maxStackSize = nugget.getMaxStackSize(nugget.getDefaultInstance());
        for (int i = amount / maxStackSize; i > 0; --i) {
            player.getInventory().placeItemBackInInventory(new ItemStack(nugget, maxStackSize));
        }
        amount %= maxStackSize;
        if (amount > 0)
            player.getInventory().placeItemBackInInventory(new ItemStack(nugget, amount));
    }

}
