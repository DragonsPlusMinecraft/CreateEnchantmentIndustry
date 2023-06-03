package plus.dragons.createenchantmentindustry.compat.quark;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.api.PrintEntryRegisterEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.Printing;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.utility.CeiLang;

import java.util.List;


public class QuarkCompat {
    public static void registerPrintEntry(){
        if(ModList.get().isLoaded("quark")){
            MinecraftForge.EVENT_BUS.addListener(QuarkCompat::register);
        }
    }

    private static void register(PrintEntryRegisterEvent event){
        event.register(new PrintEntry() {

            private final ResourceLocation id = new ResourceLocation("quark","ancient_tome");
            @Override
            public @NotNull ResourceLocation id() {
                return EnchantmentIndustry.genRL("ancient_tome");
            }

            @SuppressWarnings("all")
            @Override
            public boolean match(@NotNull ItemStack toPrint) {
                return ForgeRegistries.ITEMS.getHolder(toPrint.getItem()).get().is(id);
            }

            @Override
            public boolean valid(@NotNull ItemStack target, @NotNull ItemStack tested) {
                return tested.is(Items.ENCHANTED_BOOK);
            }

            @Override
            public int requiredInkAmount(@NotNull ItemStack target) {
                return 160;
            }

            @Override
            public boolean isTooExpensive(@NotNull ItemStack target, int limit) {
                return limit<160;
            }

            @Override
            public void addToGoggleTooltip(@NotNull List<Component> tooltip, boolean isPlayerSneaking, @NotNull ItemStack target) {
                var b = CeiLang.itemName(target).style(ChatFormatting.DARK_PURPLE);
                b.forGoggles(tooltip, 1);
                boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
                if (tooExpensive)
                    tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                            "gui.goggles.too_expensive").component()
                    ).withStyle(ChatFormatting.RED));
                else
                    tooltip.add(new TextComponent("     ").append(CeiLang.translate(
                            "gui.goggles.xp_consumption",
                            String.valueOf(requiredInkAmount(target))).component()
                    ).withStyle(ChatFormatting.GREEN));

                var e = getTomeEnchantment(target);
                if(e!=null){
                    tooltip.add(new TextComponent("     ").append(getFullTooltipText(e)).withStyle(ChatFormatting.GRAY));
                }
            }

            @Override
            public @NotNull MutableComponent getDisplaySourceContent(@NotNull ItemStack target) {
                var ret = CeiLang.itemName(target);
                var e = getTomeEnchantment(target);
                if(e!=null){
                    ret.text( " / ");
                    ret.add(getFullTooltipText(e).copy().withStyle(ChatFormatting.DARK_PURPLE));
                }
                return ret.component();
            }

            private static Enchantment getTomeEnchantment(ItemStack stack) {
                ListTag list = EnchantedBookItem.getEnchantments(stack);

                for(int i = 0; i < list.size(); ++i) {
                    CompoundTag nbt = list.getCompound(i);
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(nbt.getString("id")));
                    if (enchant != null)
                        return enchant;
                }

                return null;
            }

            public static Component getFullTooltipText(Enchantment ench) {
                return new TranslatableComponent("quark.misc.ancient_tome_tooltip", new TranslatableComponent(ench.getDescriptionId()), new TranslatableComponent("enchantment.level." + (ench.getMaxLevel() + 1)));
            }
        });
    }

}
