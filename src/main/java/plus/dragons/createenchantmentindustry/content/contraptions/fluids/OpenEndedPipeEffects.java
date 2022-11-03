package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import com.simibubi.create.content.contraptions.fluids.OpenEndedPipe;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceEffectHandler;

public class OpenEndedPipeEffects {

    public static void register() {
        OpenEndedPipe.registerEffectHandler(new ExperienceEffectHandler());
    }

}
