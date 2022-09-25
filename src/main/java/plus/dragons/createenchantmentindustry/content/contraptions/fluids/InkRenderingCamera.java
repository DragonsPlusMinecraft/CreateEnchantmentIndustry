package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import net.minecraftforge.client.event.EntityViewRenderEvent;

public interface InkRenderingCamera {
    
    boolean isInInk();
    
    static void handleInkFogColor(EntityViewRenderEvent.FogColors event) {
        if (((InkRenderingCamera)event.getCamera()).isInInk()) {
            event.setRed(0);
            event.setGreen(0);
            event.setBlue(0);
        }
    }
    
}
