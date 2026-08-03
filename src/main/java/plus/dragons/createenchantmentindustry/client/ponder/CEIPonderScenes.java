/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.client.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.data.loading.DatagenModLoader;
import plus.dragons.createenchantmentindustry.client.ponder.scene.*;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class CEIPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllItems.EXP_NUGGET)
                .addStoryBoard("experience/basic", ExperienceScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("experience/advance", ExperienceScene::advance, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES)
                .addStoryBoard("experience/prepare_for_super_enchant", ExperienceScene::prepare)
                .addStoryBoard("experience/beacon_base", ExperienceScene::beaconBase);

        HELPER.forComponents(AllBlocks.EXPERIENCE_BLOCK, CEIBlocks.SUPER_EXPERIENCE_BLOCK)
                .addStoryBoard("experience/prepare_for_super_enchant", ExperienceScene::prepare, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES);

        HELPER.forComponents(CEIBlocks.EXPERIENCE_HATCH)
                .addStoryBoard("experience_hatch", MiscScene::experienceHatch, CEIPonderTags.EXPERIENCE_APPLIANCES);

        HELPER.forComponents(CEIBlocks.MECHANICAL_GRINDSTONE)
                .addStoryBoard("grindstone/basic", GrindstoneScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("grindstone/extra", GrindstoneScene::extra);

        HELPER.forComponents(CEIBlocks.BLAZE_ENCHANTER)
                .addStoryBoard("enchanter", EnchanterScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("enchanter", EnchanterScene::superEnchant, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES)
                .addStoryBoard("automate_enchanter", EnchanterScene::automate, AllCreatePonderTags.ARM_TARGETS);

        HELPER.forComponents(CEIBlocks.BLAZE_FORGER)
                .addStoryBoard("forger", ForgerScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                .addStoryBoard("forger", ForgerScene::superEnchant, CEIPonderTags.SUPER_EXPERIENCE_APPLIANCES)
                .addStoryBoard("automate_forger", ForgerScene::automate, AllCreatePonderTags.ARM_TARGETS);

        if (DatagenModLoader.isRunningDataGen() || CEIConfig.features().classicBlazeEnchanter.get()) {
            HELPER.forComponents(CEIBlocks.CLASSIC_BLAZE_ENCHANTER)
                    .addStoryBoard("classic_blaze_enchanter", ClassicBlazeEnchanterScene::basic, CEIPonderTags.EXPERIENCE_APPLIANCES)
                    .addStoryBoard("automate_classic_blaze_enchanter", ClassicBlazeEnchanterScene::automate, AllCreatePonderTags.ARM_TARGETS);
        }

        HELPER.forComponents(CEIBlocks.PRINTER)
                .addStoryBoard("printer", MiscScene::printer, CEIPonderTags.EXPERIENCE_APPLIANCES);

        HELPER.forComponents(CEIBlocks.EXPERIENCE_LANTERN)
                .addStoryBoard("experience_lantern", MiscScene::experienceLantern, CEIPonderTags.EXPERIENCE_APPLIANCES, AllCreatePonderTags.CONTRAPTION_ACTOR);
    }

    public static void enchant(CreateSceneBuilder scene, ItemStack item, Enchantment enchantment, int level) {
        if (DatagenModLoader.isRunningDataGen()) // scene.world().getHolderLookupProvider() cause null when get level
            return;
        item.enchant(enchantment, level);
    }
}
