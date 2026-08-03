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

package plus.dragons.createenchantmentindustry.integration.apotheosis.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.Map;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.integration.ModIntegration;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.OverlimitAffixHelper;

@Restriction(require = @Condition(ModIntegration.Constants.APOTHEOSIS))
@Mixin(AffixHelper.class)
public class AffixHelperMixin {
    @Inject(method = "getAffixes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Map;", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getAffixes$applyOverlimitLevels(
            ItemStack stack,
            CallbackInfoReturnable<Map<DynamicHolder<? extends Affix>, AffixInstance>> cir) {
        cir.setReturnValue(OverlimitAffixHelper.applyTrueLevels(stack, cir.getReturnValue()));
    }
}
