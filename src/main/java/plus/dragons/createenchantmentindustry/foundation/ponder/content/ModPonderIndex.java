package plus.dragons.createenchantmentindustry.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.ModBlocks;
import plus.dragons.createenchantmentindustry.entry.ModItems;
import plus.dragons.createenchantmentindustry.foundation.ponder.ModPonderTag;

public class ModPonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(EnchantmentIndustry.MOD_ID);

    public static void register() {
        HELPER.forComponents(ModBlocks.DISENCHANTER)
                .addStoryBoard("disenchant",EnchantmentScenes::disenchant, ModPonderTag.EXPERIENCE);

        HELPER.forComponents(ModBlocks.COPIER)
                .addStoryBoard("copy",EnchantmentScenes::copy, ModPonderTag.EXPERIENCE);

        HELPER.forComponents(ModItems.ENCHANTING_GUIDE_FOR_BLAZE)
                .addStoryBoard("enchanter_transform",EnchantmentScenes::transformBlazeBurner, ModPonderTag.EXPERIENCE)
                .addStoryBoard("enchant",EnchantmentScenes::enchant, ModPonderTag.EXPERIENCE);

        HELPER.forComponents(AllItems.EXP_NUGGET)
                .addStoryBoard("experience_nugget_drop",EnchantmentScenes::dropExperienceNugget, ModPonderTag.EXPERIENCE)
                .addStoryBoard("collect_experience_nugget",EnchantmentScenes::handleExperienceNugget, ModPonderTag.EXPERIENCE)
                .addStoryBoard("experience_bottle",EnchantmentScenes::handleExperienceBottle, ModPonderTag.EXPERIENCE)
                .addStoryBoard("leak",EnchantmentScenes::leak);
    }

    public static void registerTags() {
        PonderRegistry.TAGS.forTag(ModPonderTag.EXPERIENCE)
                .add(ModBlocks.DISENCHANTER)
                .add(ModBlocks.COPIER)
                .add(ModItems.ENCHANTING_GUIDE_FOR_BLAZE)
                .add(AllItems.EXP_NUGGET)
                .add(AllBlocks.ITEM_DRAIN)
                .add(AllBlocks.SPOUT);
    }

}
