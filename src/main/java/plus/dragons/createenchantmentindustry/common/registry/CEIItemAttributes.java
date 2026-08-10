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

package plus.dragons.createenchantmentindustry.common.registry;

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import java.util.function.BiPredicate;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneHelper;

public class CEIItemAttributes {
    private static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTES = DeferredRegister
            .create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, CEICommon.ID);

    public static final RegistryObject<ItemAttributeType> PROCESSABLE_BY_MECHANICAL_GRINDSTONE = attribute("processable_by_mechanical_grindstone",
            "can be processed by Mechanical Grindstone",
            "cannot be processed by Mechanical Grindstone",
            ((itemStack, level) -> {
                var input = new SimpleContainer(itemStack);
                var recipeManager = level.getRecipeManager();
                var grinding = recipeManager.getRecipeFor(CEIRecipes.GRINDING.getType(), input, level);
                if (grinding.isPresent())
                    return true;
                var polishing = recipeManager.getRecipeFor(
                        AllRecipeTypes.SANDPAPER_POLISHING.getType(),
                        new SandPaperPolishingRecipe.SandPaperInv(itemStack),
                        level);
                if (polishing.isPresent())
                    return true;
                return GrindstoneHelper.canItemBeGrinded(itemStack, ItemStack.EMPTY);
            }));

    private static RegistryObject<ItemAttributeType> attribute(String name, String description, String invertedDescription, BiPredicate<ItemStack, Level> predicate) {
        String descriptionKey = "create.item_attributes." + CEICommon.ID + "." + name;
        String invertedDescriptionKey = descriptionKey + ".inverted";
        REGISTRATE.addRawLang(descriptionKey, description);
        REGISTRATE.addRawLang(invertedDescriptionKey, invertedDescription);
        return ITEM_ATTRIBUTES.register(name, () -> new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, predicate, CEICommon.ID + "." + name)));
    }

    public static void register(IEventBus modBus) {
        ITEM_ATTRIBUTES.register(modBus);
    }
}
