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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.client.ponder.scene.ApothicEnchantingScene;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks;

public class CEIAPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> itemHelper = helper.withKeyFunction(RegistryEntry::getId);

        itemHelper.forComponents(CEIABlocks.INFUSER)
                .addStoryBoard("infuser", ApothicEnchantingScene::infuser,
                        CEIAPonderTags.APOTHEOTIC_STATS_COMPONENTS);

        itemHelper.forComponents(CEIABlocks.BRASS_BOOKSHELF)
                .addStoryBoard("brass_bookshelf", ApothicEnchantingScene::brassBookshelf,
                        CEIAPonderTags.APOTHEOTIC_STATS_COMPONENTS, AllCreatePonderTags.KINETIC_APPLIANCES);

        itemHelper.forComponents(CEIABlocks.CREATIVE_BOOKSHELF)
                .addStoryBoard("creative_bookshelf", ApothicEnchantingScene::creativeBookshelf,
                        CEIAPonderTags.APOTHEOTIC_STATS_COMPONENTS, AllCreatePonderTags.CREATIVE);

        itemHelper.forComponents(CEIABlocks.ENDER_WOVEN_BAG)
                .addStoryBoard("ender_woven_bag", ApothicEnchantingScene::enderWovenBag)
                .addStoryBoard("ender_woven_bag_on_contraption", ApothicEnchantingScene::enderWovenBagOnContraption, AllCreatePonderTags.CONTRAPTION_ACTOR);
    }
}
