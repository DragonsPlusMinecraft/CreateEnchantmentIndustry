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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import com.mojang.serialization.DataResult;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIDyeFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class BannerPatternPrintingBehavior implements PrintingBehaviour {
    private final SmartFluidTankBehaviour tank;
    private final Holder<BannerPattern> pattern;

    public BannerPatternPrintingBehavior(SmartFluidTankBehaviour tank, Holder<BannerPattern> pattern) {
        this.tank = tank;
        this.pattern = pattern;
    }

    public static Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        if (!stack.is(ItemTags.BANNERS))
            return Optional.empty();
        ListTag patterns = BannerBlockEntity.getItemPatterns(stack);
        if (patterns.isEmpty())
            return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.banner_pattern.no_pattern")));
        if (patterns.size() > 1)
            return Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.banner_pattern.multiple_pattern")));
        Holder<BannerPattern> pattern = BannerPattern.byHash(patterns.getCompound(0).getString("Pattern"));
        return pattern == null
                ? Optional.of(DataResult.error(() -> CEICommon.asLocalization("gui.printer.banner_pattern.no_pattern")))
                : Optional.of(DataResult.success(new BannerPatternPrintingBehavior(tank, pattern)));
    }

    @Override
    public int getRequiredItemCount(Level level, ItemStack stack) {
        if (stack.is(ItemTags.BANNERS)) {
            ListTag patterns = BannerBlockEntity.getItemPatterns(stack);
            if (patterns.isEmpty())
                return 1;
            if (patterns.getCompound(patterns.size() - 1).getString("Pattern").equals(pattern.value().getHashname()))
                return 0;
            return 1;
        }
        return 0;
    }

    @Override
    public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack) {
        if (CEIDyeFluids.color(fluidStack).isEmpty())
            return 0;
        var cost = CEIDataMaps.PRINTING_BANNER_PATTERN_INGREDIENT.get(fluidStack.getFluid());
        return cost == null ? 0 : cost;
    }

    @Override
    public ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack) {
        var color = CEIDyeFluids.color(fluidStack);
        if (color.isEmpty())
            return stack;
        var result = stack.copy();
        CompoundTag blockEntityTag = result.getOrCreateTagElement("BlockEntityTag");
        ListTag patterns = blockEntityTag.getList("Patterns", Tag.TAG_COMPOUND).copy();
        CompoundTag layer = new CompoundTag();
        layer.putString("Pattern", pattern.value().getHashname());
        layer.putInt("Color", color.get().getId());
        patterns.add(layer);
        blockEntityTag.put("Patterns", patterns);
        return result;
    }

    @Override
    public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        // Plays SoundEvents.BOOK_PAGE_TURN
        level.levelEvent(1043, pos.below(), 0);
    }

    @Override
    public boolean isSafeNBT() {
        return false;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CEILang.translate("gui.goggles.printing.banner_pattern").forGoggles(tooltip);
        var fluid = tank.getPrimaryHandler().getFluid();
        var amount = CEIDataMaps.PRINTING_BANNER_PATTERN_INGREDIENT.get(fluid.getFluid());
        var color = CEIDyeFluids.color(fluid);
        if (amount != null && color.isPresent()) {
            var p = Component.literal("→ ").append(Component.translatable("block.minecraft.banner." + pattern.value().getHashname() + "." + color.get().getName())).withStyle(ChatFormatting.GOLD);
            CEILang.builder().add(p).forGoggles(tooltip, 1);
            CEILang.translate("gui.goggles.printing.cost",
                    CEILang.number(amount)
                            .add(CreateLang.translate("generic.unit.millibuckets"))
                            .style(amount <= CEIConfig.fluids().printerFluidCapacity.get()
                                    ? ChatFormatting.GREEN
                                    : ChatFormatting.RED))
                    .forGoggles(tooltip, 1);
        } else if (!fluid.isEmpty()) {
            CEILang.translate("gui.goggles.printing.incorrect_liquid").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        return true;
    }
}
