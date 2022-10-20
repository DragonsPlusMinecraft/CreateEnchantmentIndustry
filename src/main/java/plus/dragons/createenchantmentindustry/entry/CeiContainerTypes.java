package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.MenuEntry;
import plus.dragons.createdragonlib.entry.RegistrateHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.EnchantingGuideMenu;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.EnchantingGuideScreen;

public class CeiContainerTypes {

    public static final MenuEntry<EnchantingGuideMenu> ENCHANTING_GUIDE_FOR_BLAZE =
            RegistrateHelper.ContainerType.register(EnchantmentIndustry.registrate(),
                    "enchanting_guide_for_blaze", EnchantingGuideMenu::new, () -> EnchantingGuideScreen::new);

    public static void register() {
    }
}
