package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.simibubi.create.foundation.advancement.SimpleCreateTrigger;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public class SimpleTrigger extends SimpleCreateTrigger {
    private ResourceLocation id;
    
    public SimpleTrigger(ResourceLocation id) {
        super("");
        this.id = id;
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
}
