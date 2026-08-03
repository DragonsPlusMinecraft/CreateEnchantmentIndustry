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

package plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.affixEnhancer;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.AffixOperationCosts;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.AffixComposingRules;
import plus.dragons.createenchantmentindustry.integration.apotheosis.common.processing.affix.blazeComposer.OverlimitAffixHelper;
import plus.dragons.createenchantmentindustry.integration.apotheosis.config.CEIAXConfig;

public class AffixAugmenting {
    private static final Comparator<AffixInstance> TARGET_ORDER = Comparator
            .comparingDouble(AffixInstance::level)
            .thenComparing(instance -> instance.affix().getId());

    public static Optional<Result> getResult(ItemStack stack) {
        return analyze(stack).result();
    }

    public static Analysis analyze(ItemStack stack) {
        if (stack.isEmpty())
            return Analysis.emptyInput();
        float maxLevel = CEIAXConfig.server().affixes().affixAugmentorMaxLevel.getF();
        if (maxLevel <= AffixOperationCosts.EPSILON)
            return Analysis.noUpgradeableAffixes(List.of(), 0);

        List<AffixInstance> affixes = AffixHelper.streamAffixes(stack)
                .sorted(TARGET_ORDER)
                .toList();
        if (affixes.isEmpty())
            return Analysis.noAffixes();

        int validAffixes = 0;
        List<RejectedAffix> rejected = new ArrayList<>();
        for (var instance : affixes) {
            if (instance.isValid())
                validAffixes++;
        }
        Result result = null;
        for (var instance : affixes) {
            var reason = rejectionReason(instance, maxLevel);
            if (reason.isPresent()) {
                rejected.add(new RejectedAffix(instance, reason.get(), maxLevel));
                continue;
            }
            if (result == null) {
                var candidate = createResult(instance, maxLevel);
                if (candidate.result().isPresent()) {
                    result = candidate.result().get();
                } else {
                    rejected.add(new RejectedAffix(instance, candidate.rejectionReason(), maxLevel));
                }
            }
        }
        if (validAffixes == 0)
            return Analysis.noAffixes(List.copyOf(rejected));
        if (result == null)
            return Analysis.noUpgradeableAffixes(List.copyOf(rejected), validAffixes);
        return Analysis.ready(result, List.copyOf(rejected), validAffixes);
    }

    public static boolean canAugment(ItemStack stack) {
        return getResult(stack).isPresent();
    }

    public static ItemStack apply(ItemStack stack, Result result) {
        return apply(stack, result.target().affix(), result.resultLevel());
    }

    public static ItemStack apply(ItemStack stack, DynamicHolder<? extends Affix> affix, float resultLevel) {
        ItemStack output = stack.copy();
        output.setCount(1);
        OverlimitAffixHelper.setAffixLevel(output, affix, resultLevel);
        return output;
    }

    private static Optional<RejectionReason> rejectionReason(AffixInstance instance, float maxLevel) {
        if (!instance.isValid())
            return Optional.of(RejectionReason.INVALID);
        if (instance.level() >= maxLevel - AffixOperationCosts.EPSILON)
            return Optional.of(RejectionReason.AT_AUGMENTOR_CAP);
        if (AffixComposingRules.INSTANCE.deniesAugmenting(instance))
            return Optional.of(RejectionReason.DENIED_BY_RULE);
        return Optional.empty();
    }

    private static ResultCandidate createResult(AffixInstance instance, float maxLevel) {
        float currentLevel = instance.level();
        float resultLevel = Math.min(currentLevel + CEIAXConfig.server().affixes().affixTemplateMergeStep.getF(), maxLevel);
        int cost = AffixOperationCosts.augmentingCost(instance, currentLevel, resultLevel);
        if (cost <= 0)
            return ResultCandidate.rejected(RejectionReason.ZERO_COST);
        return ResultCandidate.ready(new Result(instance, currentLevel, resultLevel, cost));
    }

    private record ResultCandidate(Optional<Result> result, RejectionReason rejectionReason) {
        private static ResultCandidate ready(Result result) {
            return new ResultCandidate(Optional.of(result), RejectionReason.ZERO_COST);
        }

        private static ResultCandidate rejected(RejectionReason reason) {
            return new ResultCandidate(Optional.empty(), reason);
        }
    }

    public record Result(AffixInstance target, float currentLevel, float resultLevel, int cost) {}

    public record Analysis(
            Status status,
            Optional<Result> result,
            List<RejectedAffix> rejectedAffixes,
            int validAffixCount) {
        private static Analysis emptyInput() {
            return new Analysis(Status.EMPTY_INPUT, Optional.empty(), List.of(), 0);
        }

        private static Analysis noAffixes() {
            return noAffixes(List.of());
        }

        private static Analysis noAffixes(List<RejectedAffix> rejected) {
            return new Analysis(Status.NO_AFFIXES, Optional.empty(), rejected, 0);
        }

        private static Analysis noUpgradeableAffixes(List<RejectedAffix> rejected, int validAffixCount) {
            return new Analysis(Status.NO_UPGRADEABLE_AFFIXES, Optional.empty(), rejected, validAffixCount);
        }

        private static Analysis ready(Result result, List<RejectedAffix> rejected, int validAffixCount) {
            return new Analysis(Status.READY, Optional.of(result), rejected, validAffixCount);
        }
    }

    public record RejectedAffix(AffixInstance instance, RejectionReason reason, float maxLevel) {}

    public enum Status {
        EMPTY_INPUT,
        NO_AFFIXES,
        NO_UPGRADEABLE_AFFIXES,
        READY
    }

    public enum RejectionReason {
        INVALID,
        LEVEL_INDEPENDENT,
        AT_AUGMENTOR_CAP,
        DENIED_BY_RULE,
        ZERO_COST
    }
}
