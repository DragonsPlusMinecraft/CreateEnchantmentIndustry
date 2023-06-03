package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyVariable(method = "attack",
            at = @At("STORE"), ordinal = 3)
    private boolean enableSweepingEdgeForDeployer(boolean value){
        var self = (Player)(Object) this;
        if(self instanceof FakePlayer fakePlayer){
            ItemStack itemstack = fakePlayer.getItemInHand(InteractionHand.MAIN_HAND);
            return itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP);
        }
        return value;
    }
}
