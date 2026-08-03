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

package plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.registry;

import static plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon.REGISTRATE;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import java.util.function.BiPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.CEIACommon;
import plus.dragons.createenchantmentindustry.integration.apothic_enchanting.common.processing.infuser.InfuserBlockEntity;

public class CEIAItemAttributes {
    private static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTES = DeferredRegister
            .create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, CEIACommon.ID);

    public static final RegistryObject<ItemAttributeType> CAN_BE_INFUSED = attribute("can_be_infused",
            "can be Infused",
            "cannot be Infused",
            InfuserBlockEntity::canBeInfused);

    private static RegistryObject<ItemAttributeType> attribute(String name, String description, String invertedDescription, BiPredicate<ItemStack, Level> predicate) {
        String descriptionKey = "create.item_attributes." + CEIACommon.ID + "." + name;
        String invertedDescriptionKey = descriptionKey + ".inverted";
        REGISTRATE.addRawLang(descriptionKey, description);
        REGISTRATE.addRawLang(invertedDescriptionKey, invertedDescription);
        return ITEM_ATTRIBUTES.register(name, () -> new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, predicate, CEIACommon.ID + "." + name)));
    }

    public static void register(IEventBus modBus) {
        ITEM_ATTRIBUTES.register(modBus);
    }
}
