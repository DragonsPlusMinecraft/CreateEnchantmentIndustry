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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix;

/** Level boundaries used to preserve CEI 2.5's affix progression on Apotheosis 7.4.8. */
public final class AffixLevelLimits {
    /** Normal 1.20 Apotheosis affixes and brass templates end at level 1. */
    public static final float STANDARD_MAX_LEVEL = 1.0F;
    /** Crystal templates retain the 2.5 progression segment up to level 2. */
    public static final float EXTENDED_MAX_LEVEL = 2.0F;
    /** Apotheosis 7.4.8 itself serializes and upgrades affixes only through level 1. */
    public static final float NATIVE_STORAGE_MAX_LEVEL = 1.0F;

    private AffixLevelLimits() {}
}
