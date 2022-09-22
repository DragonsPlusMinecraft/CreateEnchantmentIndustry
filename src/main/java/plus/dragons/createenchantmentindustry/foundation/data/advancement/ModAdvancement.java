package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.utility.Components;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.foundation.mixin.CreateAdvancementConstructor;

import java.util.StringJoiner;
import java.util.function.Consumer;

public class ModAdvancement {
    protected final ResourceLocation id;
    protected final Advancement.Builder builder;
    @Nullable
    protected final SimpleTrigger builtinTrigger;
    protected final String titleKey;
    protected final String descriptionKey;
    protected final String title;
    protected final String description;
    @Nullable
    protected final CreateAdvancement createAdvancement;
    protected Advancement advancement;
    
    protected ModAdvancement(String id, Advancement.Builder builder, boolean builtin, String title, String description) {
        this.id = EnchantmentIndustry.genRL(id);
        this.builder = builder;
        if(builtin) {
            this.builtinTrigger = ModTriggers.addSimple(id + "_builtin");
            this.builder.addCriterion("builtin", builtinTrigger.instance());
        } else this.builtinTrigger = null;
        this.createAdvancement = CreateAdvancementConstructor.createInstance(id, $ -> $);
        ((ModdedCreateAdvancement)createAdvancement).fromModAdvancement(this);
        this.titleKey = new StringJoiner(".").add("advancement").add(EnchantmentIndustry.MOD_ID).add(id).toString();
        this.descriptionKey = titleKey + ".desc";this.title = title;
        this.description = description;
    }
    
    public ResourceLocation id() {
        return id;
    }
    
    public String titleKey() {
        return titleKey;
    }
    
    public String descriptionKey() {
        return descriptionKey;
    }
    
    public String title() {
        return title;
    }
    
    public String description() {
        return description;
    }
    
    @Nullable
    public SimpleTrigger getTrigger() {
        return builtinTrigger;
    }
    
    public CreateAdvancement asCreateAdvancement() {
        if(createAdvancement == null)
            throw new UnsupportedOperationException("Advancement [" + id + "] can not convert into CreateAdvancement!");
        return createAdvancement;
    }
    
    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        Advancement advancement = sp.getServer().getAdvancements().getAdvancement(id);
        if (advancement == null)
            return true;
        return sp.getAdvancements().getOrStartProgress(advancement).isDone();
    }
    
    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (builtinTrigger == null)
            throw new UnsupportedOperationException("Advancement [" + id + "] uses external Triggers, it cannot be awarded directly");
        builtinTrigger.trigger(sp);
    }
    
    public void save(Consumer<Advancement> consumer) {
        advancement = builder.save(consumer, id.toString());
    }
    
    public void appendToLang(JsonObject object) {
        object.addProperty(titleKey(), title());
        object.addProperty(descriptionKey(), description());
    }
    
    protected static class Builder {
        @Nullable
        private final ResourceLocation background;
        private final String id;
        private final Advancement.Builder builder = Advancement.Builder.advancement();
        private boolean builtin = true;
        private String title = "Untitled";
        private String description = "No Description";
        private ItemStack icon = ItemStack.EMPTY;
        private FrameType frame = FrameType.TASK;
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
        
        protected Builder parent(ResourceLocation id) {
            builder.parent(id);
            return this;
        }
        
        protected Builder parent(ModAdvancement advancement) {
            builder.parent(advancement.advancement);
            return this;
        }
    
        protected Builder transform(NonNullUnaryOperator<Advancement.Builder> transform) {
            transform.apply(builder);
            return this;
        }
    
        protected ModAdvancement build() {
            ModAdvancement advancement = new ModAdvancement(id, builder, builtin, title, description);
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
