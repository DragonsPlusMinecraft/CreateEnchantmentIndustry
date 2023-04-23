package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.contraptions.fluids.actors.FillingBySpout;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.MendingBySpout;

@Mixin(value = FillingBySpout.class, remap = false)
public class FillingBySpoutMixin {
    
    @Inject(method = "canItemBeFilled", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/fluids/actors/GenericItemFilling;canItemBeFilled(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true)
    private static void canItemBeMended(Level world, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (MendingBySpout.canItemBeMended(world, stack))
            cir.setReturnValue(true);
    }
    
    @Inject(method = "getRequiredAmountForItem", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/fluids/actors/GenericItemFilling;getRequiredAmountForItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/fluids/FluidStack;)I"), cancellable = true)
    private static void getRequiredXpAmountForItem(Level world, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<Integer> cir) {
        int amount = MendingBySpout.getRequiredAmountForItem(world, stack, availableFluid);
        if (amount > 0)
            cir.setReturnValue(amount);
    }
    
    @Inject(method = "fillItem", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/fluids/actors/GenericItemFilling;fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void mendItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = MendingBySpout.mendItem(world, requiredAmount, stack, availableFluid);
        if (result != null)
            cir.setReturnValue(result);
    }
    
}
