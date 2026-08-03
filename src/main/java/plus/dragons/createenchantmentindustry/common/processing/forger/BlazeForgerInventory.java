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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.items.ItemStackHandler;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRules;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class BlazeForgerInventory extends ItemStackHandler {
    private final BlazeForgerBlockEntity forger;
    private int cost;
    private BlazeForgerMode operation;
    private boolean conflicting;
    private boolean overCap;
    private Result result = Result.emptyInput();

    public BlazeForgerInventory(BlazeForgerBlockEntity forger) {
        super(6);
        this.forger = forger;
        this.operation = BlazeForgerMode.MERGE;
        this.conflicting = false;
        this.overCap = false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public int getSlots() {
        return 4;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot > 1)
            return stack;
        if (hasRemainingOutput())
            return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    protected void onLoad() {
        var level = forger.getLevel();
        if (level != null && !level.isClientSide)
            updateResult();
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (slot == 0 || slot == 1)
            updateResult();
        forger.notifyUpdate();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        cost = nbt.getInt("Cost");
        if (nbt.contains("Operation", Tag.TAG_INT)) {
            operation = BlazeForgerMode.BY_ID.apply(nbt.getInt("Operation"));
        } else {
            // TODO Remove this legacy fallback after pre-mode-panel Blaze Forger inventory data is no longer supported.
            operation = BlazeForgerMode.fromLegacyOperation(nbt.getInt("Mode"));
        }
        conflicting = nbt.getBoolean("Conflicting");
        overCap = nbt.getBoolean("OverCap");
        updateResult();
    }

    @Override
    public CompoundTag serializeNBT() {
        var nbt = super.serializeNBT();
        nbt.putInt("Cost", cost);
        nbt.putInt("Operation", operation.ordinal());
        nbt.putBoolean("Conflicting", conflicting);
        nbt.putBoolean("OverCap", overCap);
        return nbt;
    }

    public Result getLastResult() {
        return result;
    }

    public boolean hasRemainingOutput() {
        return !stacks.get(2).isEmpty() || !stacks.get(3).isEmpty();
    }

    protected int getExperienceCost() {
        return cost == 0 ? 0 : ExperienceHelper.getExperienceForTotalLevel(cost);
    }

    protected ItemStack extractInput(int slot, boolean simulate) {
        validateSlotIndex(slot);
        ItemStack stack = stacks.get(slot);
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        if (!simulate)
            setStackInSlot(slot, ItemStack.EMPTY);
        return stack.copy();
    }

    protected ItemStack getResult(int slot) {
        if (slot < 0 || slot >= 2) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0,2)");
        }
        return stacks.get(slot + 4);
    }

    protected void clearInput() {
        stacks.set(0, ItemStack.EMPTY);
        stacks.set(1, ItemStack.EMPTY);
        stacks.set(4, ItemStack.EMPTY);
        stacks.set(5, ItemStack.EMPTY);
        cost = 0;
        result = Result.emptyInput();
    }

    protected void clear() {
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        cost = 0;
        result = Result.emptyInput();
    }

    protected void applyResult(
            ItemStack primaryOutput,
            ItemStack secondaryOutput,
            BlazeForgerMode completedOperation,
            boolean completedConflicting,
            boolean completedOverCap,
            boolean completedSpecial) {
        if (primaryOutput.isEmpty() && secondaryOutput.isEmpty())
            return;
        stacks.set(2, primaryOutput.copy());
        stacks.set(3, secondaryOutput.copy());
        clearInput();

        forger.advancement.awardStat(CEIStats.FORGE.get(), 1);
        if (completedSpecial) {
            forger.advancement.awardStat(CEIStats.SUPER_ENCHANT.get(), 1);
            if (completedOverCap)
                forger.advancement.trigger(CEIAdvancements.TRANSCENDENT_OVERCLOCK.builtinTrigger());
            if (completedConflicting)
                forger.advancement.trigger(CEIAdvancements.PARADOX_FUSION.builtinTrigger());
        }
        forger.advancement.trigger(switch (completedOperation) {
            case MERGE -> CEIAdvancements.BLAZING_FUSION.builtinTrigger();
            case APPLY -> CEIAdvancements.SIGIL_CASTING.builtinTrigger();
            case EXTRACT -> CEIAdvancements.MAGIC_UNBINDING.builtinTrigger();
        });
    }

    protected void updateResult() {
        stacks.set(4, ItemStack.EMPTY);
        stacks.set(5, ItemStack.EMPTY);
        result = calculateResult(stacks.get(0), stacks.get(1));
        cost = result.valid() ? result.levelCost() : 0;
        operation = result.operation();
        conflicting = result.conflicting();
        overCap = result.overCap();
        if (!result.valid())
            return;
        stacks.set(4, result.primaryOutput().copy());
        stacks.set(5, result.secondaryOutput().copy());
    }

    private Result calculateResult(ItemStack baseInput, ItemStack additionInput) {
        resetComputation();
        BlazeForgerMode mode = forger.getMode();
        operation = mode;
        if (baseInput.isEmpty() && additionInput.isEmpty())
            return Result.emptyInput(mode);
        if (baseInput.isEmpty())
            return incomplete(mode, FailureReason.MISSING_FIRST_INPUT);
        if (additionInput.isEmpty())
            return incomplete(mode, FailureReason.MISSING_SECOND_INPUT);

        Result templateFailure = validateTemplateMode(baseInput, mode);
        if (templateFailure != null)
            return templateFailure;
        templateFailure = validateTemplateMode(additionInput, mode);
        if (templateFailure != null)
            return templateFailure;

        ItemStack base = single(baseInput);
        ItemStack addition = single(additionInput);
        Result modeResult = switch (mode) {
            case MERGE -> calculateMerge(base, addition);
            case APPLY -> calculateApply(base, addition);
            case EXTRACT -> calculateExtract(base, addition);
        };
        if (!modeResult.valid())
            return modeResult;

        ItemStack primaryOutput = modeResult.primaryOutput().copy();
        ItemStack secondaryOutput = modeResult.secondaryOutput().copy();
        int repairCostBefore = CEIItemData.getRepairCost(primaryOutput);
        applyRepairCost(primaryOutput, secondaryOutput);
        int repairCostAfter = CEIItemData.getRepairCost(primaryOutput);
        return Result.ready(
                mode,
                primaryOutput,
                secondaryOutput,
                cost,
                conflicting,
                overCap,
                repairCostAfter > repairCostBefore,
                repairCostBefore,
                repairCostAfter,
                modeResult.lostEnchantments());
    }

    private Result calculateMerge(ItemStack base, ItemStack addition) {
        Map<Enchantment, Integer> baseEnchantments = getEnchantments(base);
        Map<Enchantment, Integer> additionEnchantments = getEnchantments(addition);
        if (isTemplate(base) || isTemplate(addition)) {
            if (!isFilledTemplate(base) || !isFilledTemplate(addition))
                return invalid(BlazeForgerMode.MERGE, FailureReason.MERGE_REQUIRES_FILLED_TEMPLATES);
            if (!ItemStack.isSameItem(base, addition))
                return invalid(BlazeForgerMode.MERGE, FailureReason.TEMPLATE_TYPE_MISMATCH);
        } else if (base.is(Items.ENCHANTED_BOOK) || addition.is(Items.ENCHANTED_BOOK)) {
            if (!base.is(Items.ENCHANTED_BOOK) || !addition.is(Items.ENCHANTED_BOOK))
                return invalid(BlazeForgerMode.MERGE, FailureReason.MERGE_REQUIRES_MATCHING_CARRIERS);
        } else if (!ItemStack.isSameItem(base, addition)) {
            return invalid(BlazeForgerMode.MERGE, FailureReason.MERGE_REQUIRES_MATCHING_CARRIERS);
        }
        EnchantmentCombinationResult combination = combineEnchantments(base, addition, baseEnchantments, additionEnchantments);
        if (!combination.changed())
            return invalid(BlazeForgerMode.MERGE, FailureReason.WOULD_NOT_IMPROVE, combination.rejectedDescriptions());
        return Result.ready(BlazeForgerMode.MERGE, base, ItemStack.EMPTY, cost, conflicting, overCap, false, 0, 0, combination.lostDescriptions());
    }

    private Result calculateApply(ItemStack base, ItemStack addition) {
        Map<Enchantment, Integer> baseEnchantments = getEnchantments(base);
        Map<Enchantment, Integer> additionEnchantments = getEnchantments(addition);
        if (addition.getItem() instanceof EnchantingTemplateItem) {
            if (!isFilledTemplate(addition))
                return invalid(BlazeForgerMode.APPLY, FailureReason.REQUIRES_FILLED_TEMPLATE);
            if (base.getItem() instanceof EnchantingTemplateItem)
                return invalid(BlazeForgerMode.APPLY, FailureReason.APPLY_REQUIRES_TARGET_ITEM);
            if (base.is(Items.BOOK)) {
                EnchantmentBookApplicationResult book = applyEnchantmentsToBook(additionEnchantments);
                if (book.stack().isEmpty())
                    return invalid(BlazeForgerMode.APPLY, FailureReason.ENCHANTMENT_CANNOT_APPLY, book.rejectedDescriptions());
                return Result.ready(BlazeForgerMode.APPLY, book.stack(), ItemStack.EMPTY, cost, conflicting, overCap, false, 0, 0, book.lostDescriptions());
            }
            EnchantmentApplicationResult application = applyEnchantments(base, baseEnchantments, additionEnchantments);
            if (!application.changed())
                return invalid(BlazeForgerMode.APPLY, FailureReason.ENCHANTMENT_CANNOT_APPLY, application.rejectedDescriptions());
            return Result.ready(BlazeForgerMode.APPLY, base, ItemStack.EMPTY, cost, conflicting, overCap, false, 0, 0, application.lostDescriptions());
        }
        if (addition.is(Items.ENCHANTED_BOOK)) {
            if (additionEnchantments.isEmpty())
                return invalid(BlazeForgerMode.APPLY, FailureReason.REQUIRES_ENCHANTED_ADDITION);
            if (base.is(Items.BOOK) || base.getItem() instanceof EnchantingTemplateItem)
                return invalid(BlazeForgerMode.APPLY, FailureReason.APPLY_REQUIRES_TARGET_ITEM);
            EnchantmentApplicationResult application = applyEnchantments(base, baseEnchantments, additionEnchantments);
            if (!application.changed())
                return invalid(BlazeForgerMode.APPLY, FailureReason.ENCHANTMENT_CANNOT_APPLY, application.rejectedDescriptions());
            return Result.ready(BlazeForgerMode.APPLY, base, ItemStack.EMPTY, cost, conflicting, overCap, false, 0, 0, application.lostDescriptions());
        }
        return invalid(BlazeForgerMode.APPLY, FailureReason.APPLY_REQUIRES_ENCHANTED_ADDITION);
    }

    private Result calculateExtract(ItemStack base, ItemStack addition) {
        Map<Enchantment, Integer> baseEnchantments = getEnchantments(base);
        Map<Enchantment, Integer> additionEnchantments = getEnchantments(addition);
        if (!isTemplate(addition) || !additionEnchantments.isEmpty())
            return invalid(BlazeForgerMode.EXTRACT, FailureReason.REQUIRES_BLANK_TEMPLATE);
        if (baseEnchantments.isEmpty())
            return invalid(BlazeForgerMode.EXTRACT, FailureReason.SOURCE_HAS_NO_ENCHANTMENTS);
        if (!forger.special && baseEnchantments.keySet().stream().allMatch(Enchantment::isCurse))
            return invalid(BlazeForgerMode.EXTRACT, FailureReason.CURSE_EXTRACTION_REQUIRES_SUPER_MODE);
        if (base.is(Items.ENCHANTED_BOOK) && baseEnchantments.size() == 1) {
            ItemStack book = Items.BOOK.getDefaultInstance();
            var enchantment = baseEnchantments.entrySet().stream().findFirst().get();
            int level = getExtractLevel(enchantment.getKey(), baseEnchantments);
            setEnchantments(addition, Map.of(enchantment.getKey(), level));
            cost += EnchantmentProcessingRules.blazeForgerLevelCost(
                    enchantment.getKey(),
                    BlazeForgerMode.EXTRACT,
                    forger.special,
                    CEIEnchantmentHelper.anvilCost(enchantment.getKey()),
                    level);
            return Result.ready(BlazeForgerMode.EXTRACT, book, addition, cost, conflicting, overCap, false, 0, 0);
        }
        if (!extractEnchantments(base, addition, baseEnchantments))
            return invalid(BlazeForgerMode.EXTRACT, FailureReason.SOURCE_HAS_NO_EXTRACTABLE_ENCHANTMENTS);
        return Result.ready(BlazeForgerMode.EXTRACT, base, addition, cost, conflicting, overCap, false, 0, 0);
    }

    protected boolean extractEnchantments(ItemStack base, ItemStack addition, Map<Enchantment, Integer> baseEnchantments) {
        if (baseEnchantments.isEmpty())
            return false;
        var registry = Objects.requireNonNull(forger.getLevel()).registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var stream = baseEnchantments.keySet().stream().sorted(Comparator.comparingInt(registry::getId));
        if (!forger.special) {
            stream = stream.filter(enchantment -> !enchantment.isCurse());
        }
        var optional = stream.findFirst();
        if (optional.isEmpty())
            return false;
        var enchantment = optional.get();
        var removedEnchantments = new LinkedHashMap<>(baseEnchantments);
        removedEnchantments.remove(enchantment);
        setEnchantments(base, removedEnchantments);
        int level = getExtractLevel(enchantment, baseEnchantments);
        setEnchantments(addition, Map.of(enchantment, level));
        cost += EnchantmentProcessingRules.blazeForgerLevelCost(
                enchantment,
                BlazeForgerMode.EXTRACT,
                forger.special,
                CEIEnchantmentHelper.anvilCost(enchantment),
                level);
        return true;
    }

    private int getExtractLevel(Enchantment enchantment, Map<Enchantment, Integer> enchantments) {
        int level = enchantments.getOrDefault(enchantment, 0);
        if (forger.special)
            return level;
        int maxLevel = CEIEnchantmentHelper.maxLevel(enchantment);
        if (CEIConfig.enchantments().extractEnchantmentRespectLevelExtension.get())
            maxLevel += EnchantmentProcessingRules.blazeForgerLevelExtension(enchantment);
        return Math.min(level, maxLevel);
    }

    protected EnchantmentApplicationResult applyEnchantments(ItemStack base, Map<Enchantment, Integer> baseEnchantments, Map<Enchantment, Integer> additionEnchantments) {
        int cost = 0;
        var resultEnchantments = new LinkedHashMap<>(baseEnchantments);
        boolean changed = false;
        List<RejectedEnchantment> rejected = new ArrayList<>();
        for (var entry : additionEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int baseLevel = resultEnchantments.getOrDefault(enchantment, 0);
            int additionLevel = entry.getValue();
            int resultLevel = baseLevel == additionLevel ? additionLevel + 1 : Math.max(additionLevel, baseLevel);
            if (!enchantment.canApplyAtEnchantingTable(base)) {
                rejected.add(RejectedEnchantment.of(enchantment, additionLevel, RejectionReason.CANNOT_APPLY_TO_ITEM.message(base.getHoverName())));
                continue;
            }

            List<Enchantment> incompatibleEnchantments = incompatibleEnchantments(enchantment, resultEnchantments);
            if (!incompatibleEnchantments.isEmpty()) {
                if (forger.special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get()) {
                    conflicting = true;
                } else {
                    Enchantment incompatible = incompatibleEnchantments.get(0);
                    rejected.add(RejectedEnchantment.of(
                            enchantment,
                            additionLevel,
                            RejectionReason.INCOMPATIBLE_WITH_OUTPUT.message(incompatible.getFullname(resultEnchantments.getOrDefault(incompatible, 0)))));
                    continue;
                }
            }

            int maxLevel = CEIEnchantmentHelper.maxLevel(enchantment);
            int extendedMaxLevel = maxLevel + EnchantmentProcessingRules.blazeForgerLevelExtension(enchantment);

            if (resultLevel > extendedMaxLevel) {
                resultLevel = extendedMaxLevel;
            } else if (resultLevel > maxLevel && !forger.special) {
                resultLevel = maxLevel;
            }
            if (resultLevel <= baseLevel) {
                rejected.add(RejectedEnchantment.of(enchantment, additionLevel, RejectionReason.WOULD_NOT_IMPROVE.message()));
                continue;
            }
            if (resultLevel > maxLevel)
                overCap = true;

            changed = true;
            cost += EnchantmentProcessingRules.conflictExtraLevelCost() * incompatibleEnchantments.size();
            resultEnchantments.put(enchantment, resultLevel);
            int anvilCost = CEIEnchantmentHelper.anvilCost(enchantment);

            cost += EnchantmentProcessingRules.blazeForgerLevelCost(
                    enchantment,
                    forger.getMode(),
                    forger.special,
                    anvilCost,
                    resultLevel);
        }
        if (!changed)
            return new EnchantmentApplicationResult(false, rejected);
        setEnchantments(base, resultEnchantments);
        this.cost += cost;
        return new EnchantmentApplicationResult(true, rejected);
    }

    protected EnchantmentBookApplicationResult applyEnchantmentsToBook(Map<Enchantment, Integer> additionEnchantments) {
        int cost = 0;
        var resultEnchantments = new LinkedHashMap<Enchantment, Integer>();
        boolean changed = false;
        List<RejectedEnchantment> rejected = new ArrayList<>();
        for (var entry : additionEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            List<Enchantment> incompatibleEnchantments = incompatibleEnchantments(enchantment, resultEnchantments);
            if (!incompatibleEnchantments.isEmpty()) {
                if (forger.special && CEIConfig.enchantments().ignoreEnchantmentCompatibility.get()) {
                    conflicting = true;
                    cost += EnchantmentProcessingRules.conflictExtraLevelCost() * incompatibleEnchantments.size();
                } else {
                    Enchantment incompatible = incompatibleEnchantments.get(0);
                    rejected.add(RejectedEnchantment.of(
                            enchantment,
                            entry.getValue(),
                            RejectionReason.INCOMPATIBLE_WITH_OUTPUT.message(incompatible.getFullname(resultEnchantments.getOrDefault(incompatible, 0)))));
                    continue;
                }
            }
            changed = true;
            resultEnchantments.put(enchantment, entry.getValue());
            int anvilCost = CEIEnchantmentHelper.anvilCost(enchantment);
            cost += EnchantmentProcessingRules.blazeForgerLevelCost(
                    enchantment,
                    forger.getMode(),
                    forger.special,
                    anvilCost,
                    entry.getValue());
        }
        if (!changed)
            return new EnchantmentBookApplicationResult(ItemStack.EMPTY, rejected);
        ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
        setEnchantments(book, resultEnchantments);
        this.cost += cost;
        return new EnchantmentBookApplicationResult(book, rejected);
    }

    protected EnchantmentCombinationResult combineEnchantments(ItemStack base, ItemStack addition, Map<Enchantment, Integer> baseEnchantments, Map<Enchantment, Integer> additionEnchantments) {
        boolean changed = false;
        if (base.isDamaged()) {
            int baseDurability = base.getMaxDamage() - base.getDamageValue();
            int additionDurability = addition.getMaxDamage() - addition.getDamageValue();
            int fix = additionDurability + base.getMaxDamage() * 12 / 100;
            int resultDurability = baseDurability + fix;
            int resultDamage = base.getMaxDamage() - resultDurability;
            if (resultDamage < 0) {
                resultDamage = 0;
            }

            if (resultDamage < base.getDamageValue()) {
                base.setDamageValue(resultDamage);
                cost += EnchantmentProcessingRules.durabilityRepairLevelCost();
                changed = true;
            }
        }
        EnchantmentApplicationResult enchantments = applyEnchantments(base, baseEnchantments, additionEnchantments);
        return new EnchantmentCombinationResult(changed || enchantments.changed(), enchantments.rejectedEnchantments());
    }

    private List<Enchantment> incompatibleEnchantments(Enchantment enchantment, Map<Enchantment, Integer> enchantments) {
        return enchantments.keySet().stream()
                .filter(existing -> !existing.equals(enchantment))
                .filter(existing -> !enchantment.isCompatibleWith(existing))
                .toList();
    }

    protected void applyRepairCost(ItemStack base, ItemStack addition) {
        if (!forger.cursed)
            return;
        int baseCost = CEIItemData.getRepairCost(base);
        int additionCost = CEIItemData.getRepairCost(addition);
        int resultCost = AnvilMenu.calculateIncreasedRepairCost(Math.max(baseCost, additionCost));
        CEIItemData.setRepairCost(base, resultCost);
    }

    private Result validateTemplateMode(ItemStack stack, BlazeForgerMode mode) {
        if (!(stack.getItem() instanceof EnchantingTemplateItem template))
            return null;
        if (template.isSpecial() == forger.special)
            return null;
        return template.isSpecial()
                ? invalid(mode, FailureReason.SUPER_TEMPLATE_REQUIRES_SUPER_MODE)
                : invalid(mode, FailureReason.NORMAL_TEMPLATE_REQUIRES_NORMAL_MODE);
    }

    private void resetComputation() {
        cost = 0;
        operation = forger.getMode();
        conflicting = false;
        overCap = false;
    }

    private static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        return CEIItemData.getEnchantmentsForCrafting(stack);
    }

    private static void setEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (isTemplate(stack))
            CEIItemData.setStoredEnchantments(stack, enchantments);
        else
            CEIItemData.setEnchantments(stack, enchantments);
    }

    private static boolean isTemplate(ItemStack stack) {
        return stack.getItem() instanceof EnchantingTemplateItem;
    }

    private static boolean isFilledTemplate(ItemStack stack) {
        return isTemplate(stack) && !getEnchantments(stack).isEmpty();
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static Result incomplete(BlazeForgerMode mode, FailureReason reason, Object... args) {
        return Result.incomplete(mode, reason.message(args));
    }

    private static Result invalid(BlazeForgerMode mode, FailureReason reason, Object... args) {
        return Result.invalid(mode, reason.message(args));
    }

    private static Result invalid(BlazeForgerMode mode, FailureReason reason, List<Component> rejectedDescriptions, Object... args) {
        return Result.invalid(mode, reason.message(args), rejectedDescriptions);
    }

    public enum Status {
        EMPTY_INPUT,
        INCOMPLETE_INPUT,
        INVALID,
        READY
    }

    public record Result(
            Status status,
            Component failure,
            ItemStack primaryOutput,
            ItemStack secondaryOutput,
            int levelCost,
            BlazeForgerMode operation,
            boolean conflicting,
            boolean overCap,
            boolean repairCostPenalty,
            int repairCostBefore,
            int repairCostAfter,
            List<Component> lostEnchantments,
            List<Component> rejectedEnchantments) {
        public static Result emptyInput() {
            return emptyInput(BlazeForgerMode.MERGE);
        }

        public static Result emptyInput(BlazeForgerMode mode) {
            return new Result(Status.EMPTY_INPUT, Component.empty(), ItemStack.EMPTY, ItemStack.EMPTY, 0, mode, false, false, false, 0, 0, List.of(), List.of());
        }

        public static Result incomplete(BlazeForgerMode mode, Component failure) {
            return new Result(Status.INCOMPLETE_INPUT, failure, ItemStack.EMPTY, ItemStack.EMPTY, 0, mode, false, false, false, 0, 0, List.of(), List.of());
        }

        public static Result invalid(BlazeForgerMode mode, Component failure) {
            return invalid(mode, failure, List.of());
        }

        public static Result invalid(BlazeForgerMode mode, Component failure, List<Component> rejectedEnchantments) {
            return new Result(Status.INVALID, failure, ItemStack.EMPTY, ItemStack.EMPTY, 0, mode, false, false, false, 0, 0, List.of(), List.copyOf(rejectedEnchantments));
        }

        public static Result ready(
                BlazeForgerMode mode,
                ItemStack primaryOutput,
                ItemStack secondaryOutput,
                int cost,
                boolean conflicting,
                boolean overCap,
                boolean repairCostPenalty,
                int repairCostBefore,
                int repairCostAfter) {
            return ready(mode, primaryOutput, secondaryOutput, cost, conflicting, overCap, repairCostPenalty, repairCostBefore, repairCostAfter, List.of());
        }

        public static Result ready(
                BlazeForgerMode mode,
                ItemStack primaryOutput,
                ItemStack secondaryOutput,
                int cost,
                boolean conflicting,
                boolean overCap,
                boolean repairCostPenalty,
                int repairCostBefore,
                int repairCostAfter,
                List<Component> lostEnchantments) {
            return new Result(Status.READY, Component.empty(), primaryOutput, secondaryOutput, cost, mode, conflicting, overCap, repairCostPenalty, repairCostBefore, repairCostAfter, List.copyOf(lostEnchantments), List.of());
        }

        public int experienceCost() {
            return levelCost == 0 ? 0 : ExperienceHelper.getExperienceForTotalLevel(levelCost);
        }

        public boolean valid() {
            return status == Status.READY && levelCost > 0 && (!primaryOutput.isEmpty() || !secondaryOutput.isEmpty());
        }
    }

    private record EnchantmentApplicationResult(boolean changed, List<RejectedEnchantment> rejectedEnchantments) {
        private List<Component> lostDescriptions() {
            return rejectedEnchantments.stream()
                    .map(RejectedEnchantment::lostDescription)
                    .toList();
        }

        private List<Component> rejectedDescriptions() {
            return rejectedEnchantments.stream()
                    .map(RejectedEnchantment::rejectedDescription)
                    .toList();
        }
    }

    private record EnchantmentCombinationResult(boolean changed, List<RejectedEnchantment> rejectedEnchantments) {
        private List<Component> lostDescriptions() {
            return rejectedEnchantments.stream()
                    .map(RejectedEnchantment::lostDescription)
                    .toList();
        }

        private List<Component> rejectedDescriptions() {
            return rejectedEnchantments.stream()
                    .map(RejectedEnchantment::rejectedDescription)
                    .toList();
        }
    }

    private record EnchantmentBookApplicationResult(ItemStack stack, List<RejectedEnchantment> rejectedEnchantments) {
        private List<Component> lostDescriptions() {
            return rejectedEnchantments.stream()
                    .map(RejectedEnchantment::lostDescription)
                    .toList();
        }

        private List<Component> rejectedDescriptions() {
            return rejectedEnchantments.stream()
                    .map(RejectedEnchantment::rejectedDescription)
                    .toList();
        }
    }

    private record RejectedEnchantment(Enchantment enchantment, int level, Component reason) {
        private static RejectedEnchantment of(Enchantment enchantment, int level, Component reason) {
            return new RejectedEnchantment(enchantment, level, reason);
        }

        private Component lostDescription() {
            return Component.translatable(
                    "create_enchantment_industry.gui.goggles.forging.result.lost_enchantment",
                    enchantment.getFullname(level),
                    reason);
        }

        private Component rejectedDescription() {
            return Component.translatable(
                    "create_enchantment_industry.gui.goggles.forging.result.rejected_enchantment",
                    enchantment.getFullname(level),
                    reason);
        }
    }

    private enum FailureReason {
        MISSING_FIRST_INPUT("missing_first_input"),
        MISSING_SECOND_INPUT("missing_second_input"),
        NORMAL_TEMPLATE_REQUIRES_NORMAL_MODE("normal_template_requires_normal_mode"),
        SUPER_TEMPLATE_REQUIRES_SUPER_MODE("super_template_requires_super_mode"),
        MERGE_REQUIRES_MATCHING_CARRIERS("merge_requires_matching_carriers"),
        MERGE_REQUIRES_FILLED_TEMPLATES("merge_requires_filled_templates"),
        TEMPLATE_TYPE_MISMATCH("template_type_mismatch"),
        APPLY_REQUIRES_TARGET_ITEM("apply_requires_target_item"),
        APPLY_REQUIRES_ENCHANTED_ADDITION("apply_requires_enchanted_addition"),
        REQUIRES_FILLED_TEMPLATE("requires_filled_template"),
        REQUIRES_BLANK_TEMPLATE("requires_blank_template"),
        REQUIRES_ENCHANTED_ADDITION("requires_enchanted_addition"),
        SOURCE_HAS_NO_ENCHANTMENTS("source_has_no_enchantments"),
        SOURCE_HAS_NO_EXTRACTABLE_ENCHANTMENTS("source_has_no_extractable_enchantments"),
        CURSE_EXTRACTION_REQUIRES_SUPER_MODE("curse_extraction_requires_super_mode"),
        ENCHANTMENT_CANNOT_APPLY("enchantment_cannot_apply"),
        WOULD_NOT_IMPROVE("would_not_improve");

        private final String key;

        FailureReason(String key) {
            this.key = key;
        }

        public Component message(Object... args) {
            return Component.translatable("create_enchantment_industry.gui.goggles.forging.failure." + key, args);
        }
    }

    private enum RejectionReason {
        CANNOT_APPLY_TO_ITEM("cannot_apply_to_item"),
        INCOMPATIBLE_WITH_OUTPUT("incompatible_with_output"),
        WOULD_NOT_IMPROVE("would_not_improve");

        private final String key;

        RejectionReason(String key) {
            this.key = key;
        }

        public Component message(Object... args) {
            return Component.translatable("create_enchantment_industry.gui.goggles.forging.lost_reason." + key, args);
        }
    }
}
