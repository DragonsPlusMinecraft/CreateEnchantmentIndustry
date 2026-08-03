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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.kinetics.fan.salvaging;

import com.simibubi.create.foundation.recipe.RecipeApplier;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXRecipes;

public class SalvagingHelper {
    private SalvagingHelper() {}

    public static boolean canSalvage(ItemStack stack, Level level) {
        if (stack.isEmpty())
            return false;
        if (!Apotheosis.enableAdventure)
            return false;
        var input = new SimpleContainer(stack);
        if (level.getRecipeManager().getRecipeFor(CEIAXRecipes.SALVAGING.getType(), input, level).isPresent())
            return true;
        return SalvagingMenu.findMatch(level, stack) != null;
    }

    public static @Nullable List<ItemStack> salvage(ItemStack stack, Level level) {
        if (stack.isEmpty())
            return null;
        if (!Apotheosis.enableAdventure)
            return null;
        var recipeManager = level.getRecipeManager();
        var input = new SimpleContainer(stack);
        return recipeManager
                .getRecipeFor(CEIAXRecipes.SALVAGING.getType(), input, level)
                .map(recipe -> RecipeApplier.applyRecipeOn(level, stack, recipe, true))
                .or(() -> Optional.ofNullable(SalvagingMenu.findMatch(level, stack))
                        .map(recipe -> SalvagingMenu.salvageItem(level, stack)))
                .orElse(null);
    }

    public static boolean salvageItemEntity(ItemEntity entity, Level level, RandomSource random, float chance) {
        if (entity.isRemoved())
            return false;
        if (random.nextFloat() >= chance)
            return false;
        var stack = entity.getItem();
        var result = salvage(stack, level);
        if (result == null)
            return false;
        entity.discard();
        dropResults(level, entity.position(), result);
        return true;
    }

    public static int salvageEquippedItems(LivingEntity entity, Level level, RandomSource random, float chance, int maxCount) {
        if (maxCount <= 0 || random.nextFloat() >= chance)
            return 0;
        int salvaged = 0;
        var slots = getSalvageableEquipmentSlots(entity, level);
        while (salvaged < maxCount && !slots.isEmpty()) {
            var index = random.nextInt(slots.size());
            var slot = slots.remove(index);
            var stack = entity.getItemBySlot(slot);
            var result = salvage(stack, level);
            if (result == null)
                continue;
            entity.setItemSlot(slot, ItemStack.EMPTY);
            dropResults(level, entity.position(), result);
            salvaged++;
        }
        return salvaged;
    }

    public static List<EquipmentSlot> getSalvageableEquipmentSlots(LivingEntity entity, Level level) {
        var slots = new ArrayList<EquipmentSlot>();
        for (var slot : EquipmentSlot.values()) {
            if (canSalvage(entity.getItemBySlot(slot), level))
                slots.add(slot);
        }
        return slots;
    }

    public static void dropResults(Level level, Vec3 pos, List<ItemStack> results) {
        results.forEach(stack -> Containers.dropItemStack(level, pos.x, pos.y, pos.z, stack));
    }
}
