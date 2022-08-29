package plus.dragons.createenchantmentindustry.registry;

import com.tterrag.registrate.Registrate;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class ModRegistries {

    static final Registrate REGISTRATE = Registrate.create(EnchantmentIndustry.MOD_ID);

    public static void init(){
        ModFluids.register();
    }
}
