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

package plus.dragons.createenchantmentindustry.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModList;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** A stable replacement for the mismatched read/write fields in Apotheosis 7.4.8's module serializer. */
public final class CEIApotheosisModuleCondition implements ICondition {
    private static final ResourceLocation ID = CEICommon.asResource("apotheosis_module");
    private static final String APOTHEOSIS_CLASS = "dev.shadowsoffire.apotheosis.Apotheosis";

    public static final CEIApotheosisModuleCondition ENCHANTMENT = new CEIApotheosisModuleCondition(Module.ENCHANTMENT);
    public static final CEIApotheosisModuleCondition ADVENTURE = new CEIApotheosisModuleCondition(Module.ADVENTURE);

    private final Module module;

    private CEIApotheosisModuleCondition(Module module) {
        this.module = module;
    }

    public static void register() {
        CraftingHelper.register(new Serializer());
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        if (!ModList.get().isLoaded("apotheosis"))
            return false;
        try {
            Class<?> apotheosis = Class.forName(APOTHEOSIS_CLASS, false, CEIApotheosisModuleCondition.class.getClassLoader());
            Field enabled = apotheosis.getField(module.fieldName);
            return enabled.getBoolean(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to read Apotheosis module flag " + module.fieldName, exception);
        }
    }

    private enum Module {
        ENCHANTMENT("enableEnch"),
        ADVENTURE("enableAdventure");

        private final String fieldName;

        Module(String fieldName) {
            this.fieldName = fieldName;
        }

        private static Module parse(String name) {
            try {
                return valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Unknown Apotheosis module '" + name + "'", exception);
            }
        }
    }

    public static final class Serializer implements IConditionSerializer<CEIApotheosisModuleCondition> {
        @Override
        public void write(JsonObject json, CEIApotheosisModuleCondition value) {
            json.addProperty("module", value.module.name().toLowerCase(Locale.ROOT));
        }

        @Override
        public CEIApotheosisModuleCondition read(JsonObject json) {
            return new CEIApotheosisModuleCondition(Module.parse(GsonHelper.getAsString(json, "module")));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
