package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.simibubi.create.foundation.advancement.AbstractEnchantmentIndustryAdvancement;
import com.simibubi.create.foundation.utility.Components;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class EnchantmentIndustryAdvancement extends AbstractEnchantmentIndustryAdvancement {
    protected final String title;
    protected final String description;
    
    protected EnchantmentIndustryAdvancement(String id, Advancement.Builder builder, boolean builtin, String title, String description) {
        super(EnchantmentIndustry.genRL(id), builder, builtin ? ModTriggers.addSimple(id) : null);
        this.title = title;
        this.description = description;
    }
    
    @Override
    public String title() {
        return title;
    }
    
    @Override
    public String description() {
        return description;
    }
    
    protected static class Builder {
        @Nullable
        private final ResourceLocation background;
        private final String id;
        private final Advancement.Builder builder = Advancement.Builder.advancement();
        private boolean builtin = true;
        private String title = "Untitled";
        private String description = "No Description";
        private ItemStack icon;
        private FrameType frame;
        private boolean toast = true;
        private boolean announce = false;
        private boolean hide = false;
    
        protected Builder(String id) {
            this.id = id;
            this.background = "root".equals(id) ? EnchantmentIndustry.genRL("textures/gui/advancements.png") : null;
        }
    
        protected Builder title(String title) {
            this.title = title;
            return this;
        }
    
        protected Builder description(String description) {
            this.description = description;
            return this;
        }
    
        protected Builder icon(ItemStack stack) {
            this.icon = stack;
            return this;
        }
    
        protected Builder icon(ItemProviderEntry<?> item) {
            return icon(item.asStack());
        }
    
        protected Builder icon(ItemLike item) {
            return icon(new ItemStack(item));
        }
    
        protected Builder frame(FrameType frame) {
            this.frame = frame;
            return this;
        }
    
        protected Builder toast(boolean bl) {
            this.toast = bl;
            return this;
        }
    
        protected Builder announce(boolean bl) {
            this.announce = bl;
            return this;
        }
    
        protected Builder hidden() {
            this.hide = true;
            return this;
        }
    
        protected Builder externalTrigger(String key, CriterionTriggerInstance trigger) {
            builder.addCriterion(key, trigger);
            this.builtin = false;
            return this;
        }
    
        protected Builder transform(NonNullUnaryOperator<Advancement.Builder> transform) {
            transform.apply(builder);
            return this;
        }
    
        protected EnchantmentIndustryAdvancement build() {
            EnchantmentIndustryAdvancement advancement = new EnchantmentIndustryAdvancement(id, builder, builtin, title, description);
            builder.display(
                icon,
                Components.translatable(advancement.titleKey),
                Components.translatable(advancement.descriptionKey),
                background,
                frame,
                toast,
                announce,
                hide
            );
            ModAdvancements.ENTRIES.add(advancement);
            return advancement;
        }
        
    }
    
}
