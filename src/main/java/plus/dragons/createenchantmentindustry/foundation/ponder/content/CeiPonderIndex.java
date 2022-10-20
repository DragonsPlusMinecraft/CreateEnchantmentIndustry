package plus.dragons.createenchantmentindustry.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.foundation.ponder.CeiPonderTag;

public class CeiPonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(EnchantmentIndustry.MOD_ID);

    public static void register() {
        HELPER.forComponents(CeiBlocks.DISENCHANTER)
                .addStoryBoard("disenchant", EnchantmentScenes::disenchant, CeiPonderTag.EXPERIENCE);

        HELPER.forComponents(CeiBlocks.COPIER)
                .addStoryBoard("copy", EnchantmentScenes::copy, CeiPonderTag.EXPERIENCE);

        HELPER.forComponents(CeiItems.ENCHANTING_GUIDE)
                .addStoryBoard("enchanter_transform", EnchantmentScenes::transformBlazeBurner, CeiPonderTag.EXPERIENCE)
                .addStoryBoard("enchant", EnchantmentScenes::enchant, CeiPonderTag.EXPERIENCE)
                .addStoryBoard("hyper_enchant", EnchantmentScenes::hyperEnchant, CeiPonderTag.EXPERIENCE);

        HELPER.forComponents(AllItems.EXP_NUGGET)
                .addStoryBoard("experience_nugget_drop", EnchantmentScenes::dropExperienceNugget, CeiPonderTag.EXPERIENCE)
                .addStoryBoard("collect_experience_nugget", EnchantmentScenes::handleExperienceNugget, CeiPonderTag.EXPERIENCE)
                .addStoryBoard("experience_bottle", EnchantmentScenes::handleExperienceBottle, CeiPonderTag.EXPERIENCE)
                .addStoryBoard("leak", EnchantmentScenes::leak);
    }

    public static void registerTags() {
        PonderRegistry.TAGS.forTag(CeiPonderTag.EXPERIENCE)
                .add(CeiBlocks.DISENCHANTER)
                .add(CeiBlocks.COPIER)
                .add(CeiItems.ENCHANTING_GUIDE)
                .add(AllItems.EXP_NUGGET)
                .add(CeiItems.HYPER_EXP_BOTTLE)
                .add(AllBlocks.ITEM_DRAIN)
                .add(AllBlocks.SPOUT);
    }

}
