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

package plus.dragons.createenchantmentindustry.common.kinetics.deployer;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent.XpChange;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@EventBusSubscriber
public class DeployerExtension {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingExperienceDrop(final LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof DeployerFakePlayer deployer))
            return;
        int experience = Mth.ceil(event.getDroppedExperience() * CEIConfig.kinetics().deployerKillXpScale.getF());
        event.setDroppedExperience(experience);
        if (CEIConfig.kinetics().deployerCollectXp.get()) {
            deployer.giveExperiencePoints(experience);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockDrops(final BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof DeployerFakePlayer deployer))
            return;
        boolean dropXp = CEIConfig.kinetics().deployerMineDropXp.get();
        int experience = Mth.ceil(event.getDroppedExperience() * CEIConfig.kinetics().deployerMineXpScale.getF());
        if (CEIConfig.kinetics().deployerCollectXp.get()) {
            deployer.giveExperiencePoints(experience);
            dropXp = false;
        }
        if (dropXp) {
            event.getState().getBlock().popExperience(event.getLevel(), event.getPos(), experience);
        }
        event.getDrops().stream()
                .map(ItemEntity::getItem)
                .forEach(deployer.getInventory()::placeItemBackInInventory);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onXpChange(final XpChange event) {
        if (!(event.getEntity() instanceof DeployerFakePlayer deployer))
            return;
        if (!CEIConfig.kinetics().deployerCollectXp.get())
            return;
        int total = deployer.totalExperience + event.getAmount();
        int consumed = 0;
        if (CEIConfig.kinetics().deployerMendItem.get()) {
            ItemStack heldItem = deployer.getMainHandItem();
            if (ExperienceHelper.canRepairItem(heldItem))
                consumed = ExperienceHelper.repairItem(total, deployer.serverLevel(), heldItem, false);
        }
        int nuggets = (event.getAmount() - consumed) / 3;
        if (nuggets > 0) {
            deployer.getInventory().placeItemBackInInventory(AllItems.EXP_NUGGET.asStack(nuggets));
        }
        event.setAmount(total - consumed - nuggets * 3);
    }
}
