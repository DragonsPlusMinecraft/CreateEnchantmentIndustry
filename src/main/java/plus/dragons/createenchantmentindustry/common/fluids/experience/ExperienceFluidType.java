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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import java.util.function.Supplier;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ExperienceFluidType extends SolidRenderFluidType {
    protected ExperienceFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, Vector3f fogColor, Supplier<Float> fogDistanceModifier) {
        super(properties, stillTexture, flowingTexture, tintColor, fogColor, fogDistanceModifier);
    }

    public static FluidTypeFactory create() {
        Vector3f fogColor = new Color(0x52b64c).asVectorF();
        return (properties, stillTexture, flowingTexture) -> new ExperienceFluidType(properties,
                stillTexture,
                flowingTexture,
                NO_TINT,
                fogColor,
                ExperienceFluidType::getExperienceFluidVisibility);
    }

    private static float getExperienceFluidVisibility() {
        return CEIConfig.client().experienceVisionMultiplier.getF() / 32f;
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return CEIConfig.server().fluids.experienceVaporizeOnPlacement.get();
    }

    @Override
    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
        level.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, pos.getCenter(), ExperienceHelper.getExperienceFromFluid(stack));
        }
    }
}
