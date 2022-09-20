package plus.dragons.createenchantmentindustry.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
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
                .addStoryBoard("disenchant",EnchantmentScenes::disenchant, ModPonderTag.EXPERIENCE)
                .addStoryBoard("experience_bottle",EnchantmentScenes::handleExperienceBottle, ModPonderTag.EXPERIENCE)
                .addStoryBoard("leak",EnchantmentScenes::leak);

        HELPER.forComponents(ModBlocks.COPIER)
                .addStoryBoard("copy",EnchantmentScenes::copy, ModPonderTag.EXPERIENCE);

        HELPER.forComponents(ModItems.ENCHANTING_GUIDE_FOR_BLAZE)
                .addStoryBoard("alter_transform",EnchantmentScenes::transformBlazeBurner, ModPonderTag.EXPERIENCE)
                .addStoryBoard("alter_enchant",EnchantmentScenes::enchant, ModPonderTag.EXPERIENCE);
    }

    public static void registerTags() {
        PonderRegistry.TAGS.forTag(ModPonderTag.EXPERIENCE)
                .add(ModBlocks.DISENCHANTER)
                .add(ModBlocks.COPIER)
                .add(ModItems.ENCHANTING_GUIDE_FOR_BLAZE)
                .add(AllBlocks.ITEM_DRAIN)
                .add(AllBlocks.SPOUT);
    }

}
