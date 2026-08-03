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

import static com.simibubi.create.infrastructure.ponder.AllCreatePonderTags.*;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry.CEIABlocks;

public class CEIAPonderTags {
    public static final ResourceLocation APOTHEOTIC_STATS_COMPONENTS = CEICommon.asResource("apotheotic_stats_components");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?>> entryHelper = helper.withKeyFunction(RegistryEntry::getId);

        helper.registerTag(APOTHEOTIC_STATS_COMPONENTS)
                .addToIndex()
                .item(CEIABlocks.INFUSER.get(), true, false)
                .title("Apotheotic Stats Components")
                .description("Components which generate or relay on Apotheotic Stats")
                .register();

        entryHelper.addToTag(APOTHEOTIC_STATS_COMPONENTS)
                .add(CEIABlocks.INFUSER)
                .add(CEIABlocks.BRASS_BOOKSHELF)
                .add(CEIABlocks.CREATIVE_BOOKSHELF);

        entryHelper.addToTag(CREATIVE)
                .add(CEIABlocks.CREATIVE_BOOKSHELF);

        entryHelper.addToTag(KINETIC_APPLIANCES)
                .add(CEIABlocks.BRASS_BOOKSHELF);

        entryHelper.addToTag(CONTRAPTION_ACTOR)
                .add(CEIABlocks.ENDER_WOVEN_BAG);
    }
}
