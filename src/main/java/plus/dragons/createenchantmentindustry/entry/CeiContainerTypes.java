package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.MenuEntry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideMenu;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideScreen;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiContainerTypes {

    public static final MenuEntry<EnchantingGuideMenu> ENCHANTING_GUIDE_FOR_BLAZE = REGISTRATE.menu(
        "enchanting_guide_for_blaze",
        EnchantingGuideMenu::new,
        () -> EnchantingGuideScreen::new
    ).register();

    public static void register() {
    }
}
