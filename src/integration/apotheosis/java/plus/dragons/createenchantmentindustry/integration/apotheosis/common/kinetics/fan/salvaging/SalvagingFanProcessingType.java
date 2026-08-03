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

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import java.util.List;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXBlocks;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.registry.CEIAXFluids;
import plus.dragons.createenchantmentindustry.integration.apotheosis.config.CEIAXConfig;

public class SalvagingFanProcessingType implements FanProcessingType {
    public SalvagingFanProcessingType() {}

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        var fluidState = level.getFluidState(pos);
        if (fluidState.is(CEIAXFluids.MOD_TAGS.fanSalvagingCatalysts))
            return true;
        var state = level.getBlockState(pos);
        return state.is(CEIAXBlocks.MOD_TAGS.fanSalvagingCatalysts);
    }

    @Override
    public int getPriority() {
        return 350; // Should be greater than Bulk Haunting and smaller than Bulk Washing
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        return SalvagingHelper.canSalvage(stack, level);
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        return SalvagingHelper.salvage(stack, level);
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.random.nextInt(8) == 0) {
            level.addParticle(
                    ParticleTypes.DRAGON_BREATH,
                    pos.x + (level.random.nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.random.nextFloat() - .5f) * .5f,
                    0, 2f, 0);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        particleAccess.setColor(Color.mixColors(0xB700D2, 0xF90070, random.nextFloat()));
        particleAccess.setAlpha(1f);
        if (random.nextFloat() < 1 / 32f)
            particleAccess.spawnExtraParticle(ParticleTypes.DRAGON_BREATH, 0f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide)
            return;
        if (entity instanceof LivingEntity livingEntity) {
            SalvagingHelper.salvageEquippedItems(livingEntity, level, livingEntity.getRandom(), CEIAXConfig.server().utility().bulkSalvagingSalvageEquippedItemProbability.getF(), 1);
            if (livingEntity.isAffectedByPotions() && entity.tickCount % 5 == 0) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
            }

        }
    }
}
