package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.AllItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.api.PrintEntryRegisterEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.utility.CeiLang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintEntries {
    public static Map<ResourceLocation,PrintEntry> ENTRIES = new HashMap<>();

    static{
        var e1 = new EnchantedBook();
        var e2 = new WrittenBook();
        var e3 = new NameTag();
        var e4 = new Schedule();
        ENTRIES.put(e1.id(),e1);
        ENTRIES.put(e2.id(),e2);
        ENTRIES.put(e3.id(),e3);
        ENTRIES.put(e4.id(),e4);

        var event = new PrintEntryRegisterEvent();
        MinecraftForge.EVENT_BUS.post(event);
    }

    static class EnchantedBook implements PrintEntry{

        @Override
        public ResourceLocation id() {
            return EnchantmentIndustry.genRL("enchanted_book");
        }

        @Override
        public boolean match(ItemStack toPrint) {
            return toPrint.is(Items.ENCHANTED_BOOK);
        }

        @Override
        public boolean valid(ItemStack target, ItemStack tested) {
            return tested.is(Items.BOOK);
        }

        @Override
        public int requiredInkAmount(ItemStack target) {
            return getExperienceFromItem(target);
        }

        @Override
        public boolean isTooExpensive(ItemStack target, int limit) {
            return getExperienceFromItem(target) > limit;
        }

        @Override
        public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
            var b = CeiLang.itemName(target).style(ChatFormatting.LIGHT_PURPLE);
            b.forGoggles(tooltip, 1);
            boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
            if (tooExpensive)
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.too_expensive").component()
                ).withStyle(ChatFormatting.RED));
            else
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.xp_consumption",
                        String.valueOf(getExperienceFromItem(target))).component()
                ).withStyle(ChatFormatting.GREEN));
            var map = EnchantmentHelper.getEnchantments(target);
            for (var e : map.entrySet()) {
                Component name = e.getKey().getFullname(e.getValue());
                tooltip.add(new TextComponent("     ").append(name).withStyle(name.getStyle()));
            }
        }

        @Override
        public MutableComponent getDisplaySourceContent(ItemStack target) {
            var ret = CeiLang.itemName(target).text( " / ");
            var map = EnchantmentHelper.getEnchantments(target);
            for (var e : map.entrySet()) {
                Component name = e.getKey().getFullname(e.getValue());
                ret.add(name.copy()).text(" ");
            }
            return ret.component();
        }

        public static int getExperienceFromItem(ItemStack itemStack) {
            return EnchantmentHelper.getEnchantments(itemStack)
                    .entrySet()
                    .stream()
                    .map(entry -> Enchanting.getExperienceConsumption(entry.getKey(), entry.getValue()))
                    .reduce(0, Integer::sum);
        }
    }
    static class WrittenBook implements PrintEntry{

        @Override
        public ResourceLocation id() {
            return EnchantmentIndustry.genRL("written_book");
        }

        @Override
        public boolean match(ItemStack toPrint) {
            return toPrint.is(Items.WRITTEN_BOOK);
        }

        @Override
        public boolean valid(ItemStack target, ItemStack tested) {
            return tested.is(Items.BOOK);
        }

        @Override
        public int requiredInkAmount(ItemStack target) {
            return WrittenBookItem.getPageCount(target) * CeiConfigs.SERVER.copyWrittenBookCostPerPage.get();
        }

        @Override
        public Fluid requiredInkType() {
            return CeiFluids.INK.get();
        }

        @Override
        public ItemStack print(ItemStack target) {
            var ret = target.copy();
            target.getOrCreateTag().putInt("generation", 0);
            return ret;
        }

        @Override
        public boolean isTooExpensive(ItemStack target, int limit) {
            return WrittenBookItem.getPageCount(target) * CeiConfigs.SERVER.copyWrittenBookCostPerPage.get() > limit;
        }

        @Override
        public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
            var page = WrittenBookItem.getPageCount(target);
            var b = CeiLang.builder()
                    .add(CeiLang.itemName(target)
                            .style(ChatFormatting.BLUE))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CeiLang.number(page)
                            .text(" ")
                            .add(page == 1 ? CeiLang.translate("generic.unit.page") : CeiLang.translate("generic.unit.pages"))
                            .style(ChatFormatting.DARK_GRAY));
            b.forGoggles(tooltip, 1);
            if (Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get()))
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.too_expensive").component()
                ).withStyle(ChatFormatting.RED));
            else
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.ink_consumption",
                        String.valueOf(CeiConfigs.SERVER.copyWrittenBookCostPerPage.get() * page)).component()
                ).withStyle(ChatFormatting.DARK_GRAY));
        }

        @Override
        public MutableComponent getDisplaySourceContent(ItemStack target) {
            var page = WrittenBookItem.getPageCount(target);
            return CeiLang.builder()
                    .add(CeiLang.itemName(target))
                    .text( " / ")
                    .add(CeiLang.number(page)
                            .text(" ")
                            .add(page == 1 ? CeiLang.translate("generic.unit.page") : CeiLang.translate("generic.unit.pages"))).component();
        }
    }

    static class NameTag implements PrintEntry{

        @Override
        public ResourceLocation id() {
            return EnchantmentIndustry.genRL("name_tag");
        }

        @Override
        public boolean match(ItemStack toPrint) {
            return toPrint.is(Items.NAME_TAG);
        }

        @Override
        public boolean valid(ItemStack target, ItemStack tested) {
            return  tested.is(target.getItem()) && !ItemStack.tagMatches(target, tested);
        }

        @Override
        public int requiredInkAmount(ItemStack target) {
            return CeiConfigs.SERVER.copyNameTagCost.get();
        }

        @Override
        public boolean isTooExpensive(ItemStack target, int limit) {
            return CeiConfigs.SERVER.copyNameTagCost.get() > limit;
        }

        @Override
        public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
            var b = CeiLang.builder()
                    .add(new TranslatableComponent(target.getDescriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .text(ChatFormatting.GREEN, " / ")
                    .add(CeiLang.itemName(target)
                            .style(ChatFormatting.GREEN));
            b.forGoggles(tooltip, 1);
            boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
            if (tooExpensive)
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.too_expensive").component()
                ).withStyle(ChatFormatting.RED));
            else
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.xp_consumption",
                        String.valueOf(CeiConfigs.SERVER.copyNameTagCost.get())).component()
                ).withStyle(ChatFormatting.GREEN));
        }

        @Override
        public MutableComponent getDisplaySourceContent(ItemStack target) {
            return CeiLang.builder()
                    .add(new TranslatableComponent(target.getDescriptionId()))
                    .text(" / ")
                    .add(CeiLang.itemName(target)).component();
        }
    }

    static class Schedule implements PrintEntry{

        @Override
        public ResourceLocation id() {
            return EnchantmentIndustry.genRL("schedule");
        }

        @Override
        public boolean match(ItemStack toPrint) {
            return toPrint.is(AllItems.SCHEDULE.get());
        }

        @Override
        public boolean valid(ItemStack target, ItemStack tested) {
            return tested.is(target.getItem()) && !ItemStack.tagMatches(target, tested);
        }

        @Override
        public int requiredInkAmount(ItemStack target) {
            return CeiConfigs.SERVER.copyTrainScheduleCost.get();
        }

        @Override
        public Fluid requiredInkType() {
            return CeiFluids.INK.get();
        }

        @Override
        public boolean isTooExpensive(ItemStack target, int limit) {
            return CeiConfigs.SERVER.copyTrainScheduleCost.get() > limit;
        }

        @Override
        public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
            var b = CeiLang.itemName(target).style(ChatFormatting.BLUE);
            b.forGoggles(tooltip, 1);
            boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
            if (tooExpensive)
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.too_expensive").component()
                ).withStyle(ChatFormatting.RED));
            else
                tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                        "gui.goggles.ink_consumption",
                        String.valueOf(CeiConfigs.SERVER.copyTrainScheduleCost.get())).component()
                ).withStyle(ChatFormatting.DARK_GRAY));
        }

        @Override
        public MutableComponent getDisplaySourceContent(ItemStack target) {
            return CeiLang.itemName(target).component();
        }
    }

}
