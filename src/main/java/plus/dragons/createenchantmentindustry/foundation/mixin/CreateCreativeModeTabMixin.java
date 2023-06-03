package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.infrastructure.item.CreateCreativeModeTab;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.api.event.FillCreateItemGroupEvent;

@Mixin(CreateCreativeModeTab.class)
public class CreateCreativeModeTabMixin {
    
    @Inject(method = "fillItemList", at = @At("TAIL"))
    private void postFillCreateItemGroupEvent(NonNullList<ItemStack> items, CallbackInfo ci) {
        var event = new FillCreateItemGroupEvent((CreateCreativeModeTab) (Object) this, items);
        MinecraftForge.EVENT_BUS.post(event);
        event.apply();
    }
    
}
