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

package plus.dragons.createenchantmentindustry.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BlazeLightningHelper {
    public static final String LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY = "ExperienceCharge";
    public static final TagKey<Block> LIGHTNING_ROD_BLOCKS = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("forge", "lightning_rods"));
    public static final TagKey<PoiType> LIGHTNING_ROD_POINT_OF_INTEREST_TYPES = TagKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            new ResourceLocation("forge", "lightning_rods"));

    private BlazeLightningHelper() {}

    public static @Nullable BlockPos getStrikePos(Level level, BlockPos source) {
        var dimension = level.dimensionType();
        if (!dimension.hasSkyLight())
            return null;
        if (dimension.hasCeiling())
            return null;
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, source).below();
    }

    public static boolean isStrikeBlocked(BlockPos source, @Nullable BlockPos strikePos) {
        return !source.equals(strikePos);
    }

    @SuppressWarnings("all")
    public static boolean strikeLightning(ServerLevel level, BlockPos strikePos) {
        var lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null)
            return false;
        lightning.getPersistentData().putBoolean(LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY, true);
        Optional<BlockPos> rodPos = level.getPoiManager().findAll(
                poi -> poi.is(LIGHTNING_ROD_POINT_OF_INTEREST_TYPES),
                pos -> pos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1,
                strikePos,
                128,
                PoiManager.Occupancy.ANY).unordered().findAny();
        if (rodPos.isEmpty())
            rodPos = findTaggedLightningRod(level, strikePos, 128);
        lightning.moveTo(Vec3.atBottomCenterOf(rodPos.orElse(strikePos).above()));
        level.addFreshEntity(lightning);
        return rodPos.isEmpty();
    }

    private static Optional<BlockPos> findTaggedLightningRod(ServerLevel level, BlockPos center, int radius) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                int distance = (x - center.getX()) * (x - center.getX())
                        + (z - center.getZ()) * (z - center.getZ());
                if (distance > radius * radius || distance >= closestDistance)
                    continue;
                mutable.set(x, center.getY(), z);
                if (!level.hasChunkAt(mutable))
                    continue;
                mutable.setY(level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1);
                if (level.getBlockState(mutable).is(LIGHTNING_ROD_BLOCKS)) {
                    closest = mutable.immutable();
                    closestDistance = distance;
                }
            }
        }
        return Optional.ofNullable(closest);
    }
}
