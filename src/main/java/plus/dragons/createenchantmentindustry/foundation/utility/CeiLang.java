package plus.dragons.createenchantmentindustry.foundation.utility;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.foundation.utility.LangNumberFormat;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;


public class CeiLang {

    public static LangBuilder builder() {
        return new CeiLangBuilder(EnchantmentIndustry.ID);
    }


    public static LangBuilder blockName(BlockState state) {
        return builder().add(state.getBlock()
                .getName());
    }

    public static LangBuilder itemName(ItemStack stack) {
        return builder().add(stack.getHoverName()
                .copy());
    }

    public static LangBuilder fluidName(FluidStack stack) {
        return builder().add(stack.getDisplayName()
                .copy());
    }

    public static LangBuilder fromRL(String category, ResourceLocation loc, Object... args) {
        return builder().add(Components.translatable(Util.makeDescriptionId(category, loc), args));
    }
    
    public static LangBuilder tooltip(Item item, String suffix, Object... args) {
        return builder().add(Components.translatable(item.getDescriptionId() + ".tooltip." + suffix, args));
    }

    public static LangBuilder number(double d) {
        return builder().text(LangNumberFormat.format(d));
    }

    public static LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    public static LangBuilder text(String text) {
        return builder().text(text);
    }

}
