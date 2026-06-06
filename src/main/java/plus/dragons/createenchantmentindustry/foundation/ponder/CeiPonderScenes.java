package plus.dragons.createenchantmentindustry.foundation.ponder;

import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class CeiPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(CeiBlocks.DISENCHANTER)
                .addStoryBoard("disenchant", EnchantmentScenes::disenchant, CeiPonderTags.EXPERIENCE);

        HELPER.forComponents(CeiBlocks.PRINTER)
                .addStoryBoard("copy", EnchantmentScenes::copy, CeiPonderTags.EXPERIENCE);

        HELPER.forComponents(CeiItems.ENCHANTING_GUIDE)
                .addStoryBoard("enchanter_transform", EnchantmentScenes::transformBlazeBurner, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("enchant", EnchantmentScenes::enchant, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("hyper_enchant", EnchantmentScenes::hyperEnchant, CeiPonderTags.EXPERIENCE);

        HELPER.forComponents(AllItems.EXP_NUGGET)
                .addStoryBoard("collect_experience_nugget", EnchantmentScenes::handleExperienceNugget, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("experience_bottle", EnchantmentScenes::handleExperienceBottle, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("leak", EnchantmentScenes::leak)
                .addStoryBoard("experience_nugget_drop", EnchantmentScenes::dropExperienceNugget, CeiPonderTags.EXPERIENCE)
                .addStoryBoard("crushing_wheel_tweak", EnchantmentScenes::crushingWheelTweak, CeiPonderTags.EXPERIENCE);
    }
}
