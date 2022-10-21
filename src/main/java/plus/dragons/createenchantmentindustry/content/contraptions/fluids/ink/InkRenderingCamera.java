package plus.dragons.createenchantmentindustry.content.contraptions.fluids.ink;

import net.minecraftforge.client.event.ViewportEvent;

public interface InkRenderingCamera {

    boolean isInInk();

    static void handleInkFogColor(ViewportEvent.ComputeFogColor event) {
        if (((InkRenderingCamera) event.getCamera()).isInInk()) {
            event.setRed(0);
            event.setGreen(0);
            event.setBlue(0);
        }
    }

}
