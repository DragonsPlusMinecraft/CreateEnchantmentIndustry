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

package plus.dragons.createenchantmentindustry.common.processing;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import plus.dragons.createenchantmentindustry.client.CEIClient;

/** Common-side item that asks the physical client to install its custom renderer. */
public abstract class BlazeCustomRenderedBlockItem extends BlockItem {
    protected BlazeCustomRenderedBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        CEIClient.initializeBlazeItemRenderer(this, getRenderer(), consumer);
    }

    protected abstract Renderer getRenderer();

    public static final class Enchanter extends BlazeCustomRenderedBlockItem {
        public Enchanter(Block block, Item.Properties properties) {
            super(block, properties);
        }

        @Override
        protected Renderer getRenderer() {
            return Renderer.ENCHANTER;
        }
    }

    public static final class Forger extends BlazeCustomRenderedBlockItem {
        public Forger(Block block, Item.Properties properties) {
            super(block, properties);
        }

        @Override
        protected Renderer getRenderer() {
            return Renderer.FORGER;
        }
    }

    public static final class ClassicEnchanter extends BlazeCustomRenderedBlockItem {
        public ClassicEnchanter(Block block, Item.Properties properties) {
            super(block, properties);
        }

        @Override
        protected Renderer getRenderer() {
            return Renderer.CLASSIC_ENCHANTER;
        }
    }

    public enum Renderer {
        ENCHANTER,
        FORGER,
        CLASSIC_ENCHANTER
    }
}
