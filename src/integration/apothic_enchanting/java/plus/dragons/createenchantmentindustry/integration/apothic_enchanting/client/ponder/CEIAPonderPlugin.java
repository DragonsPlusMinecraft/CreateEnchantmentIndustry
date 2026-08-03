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

import dev.shadowsoffire.apotheosis.Apotheosis;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;

public class CEIAPonderPlugin implements PonderPlugin {
    public static final List<Consumer<PonderSceneRegistrationHelper<ResourceLocation>>> SCENES = new ArrayList<>();
    public static final List<Consumer<PonderTagRegistrationHelper<ResourceLocation>>> TAGS = new ArrayList<>();

    @Override
    public String getModId() {
        return CEIACommon.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        if (Apotheosis.enableEnch)
            CEIAPonderScenes.register(helper);
        for (var scene : SCENES)
            scene.accept(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        if (Apotheosis.enableEnch)
            CEIAPonderTags.register(helper);
        for (var tag : TAGS)
            tag.accept(helper);
    }
}
