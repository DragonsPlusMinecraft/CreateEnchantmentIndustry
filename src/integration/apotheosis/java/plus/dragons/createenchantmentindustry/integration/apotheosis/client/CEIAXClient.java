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

package plus.dragons.createenchantmentindustry.integration.apotheosis.client;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.BlazeComposerItemRenderer;

/** Physical-client bridge for renderers referenced by common-side item classes. */
public final class CEIAXClient {
    private CEIAXClient() {}

    public static void initializeBlazeComposerItemRenderer(
            Item item, Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(item, new BlazeComposerItemRenderer()));
    }
}
