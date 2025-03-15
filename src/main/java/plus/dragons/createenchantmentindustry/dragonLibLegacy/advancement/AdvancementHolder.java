package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.advancements.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.SimpleTrigger;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.TriggerFactory;
import plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy.CreateAdvancementConstructor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AdvancementHolder {

    public static final Map<String, List<AdvancementHolder>> ENTRIES_MAP = new HashMap<>();
    protected final ResourceLocation id;
    protected final Builder builder;
    protected final Advancement.Builder mcBuilder;
    @Nullable
    protected final SimpleTrigger builtinTrigger;
    protected final String titleKey;
    protected final String descriptionKey;
    protected final String title;
    protected final String description;
    @Nullable
    protected final AdvancementHolder parent;
    @Nullable
    protected final CreateAdvancement createAdvancement;
    protected net.minecraft.advancements.AdvancementHolder advancement;

    protected AdvancementHolder(Builder b) {
        this.id = ResourceLocation.fromNamespaceAndPath(b.modid, b.id);
        this.builder = b;
        this.mcBuilder = b.builder;
        this.parent = b.parent;
        if(b.builtin) {
            this.builtinTrigger = b.factory.simple(ResourceLocation.fromNamespaceAndPath(b.modid, "builtin/" + b.id));
            this.mcBuilder.addCriterion("builtin", builtinTrigger.createCriterion(builtinTrigger.instance()));
        } else this.builtinTrigger = null;
        this.createAdvancement = CreateAdvancementConstructor.createInstance(b.id, $ -> $);
        ((CreateAdvancementAccess) createAdvancement).fromAdvancementHolder(this);
        this.titleKey = new StringJoiner(".").add("advancement").add(b.modid).add(b.id).toString();
        this.descriptionKey = titleKey + ".desc";
        this.title = b.title;
        this.description = b.description;
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
        var advancement = sp.getServer().getAdvancements().get(id);
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
    
    public void save(Consumer<net.minecraft.advancements.AdvancementHolder> consumer, HolderLookup.Provider provider) {
        if (parent != null) mcBuilder.parent(parent.advancement);

        if (builder.iconFunc != null)
            builder.icon(builder.iconFunc.apply(provider));

        mcBuilder.display(
                builder.icon,
                Component.translatable(titleKey),
                Component.translatable(descriptionKey).withStyle(s -> s.withColor(0xDBA213)),
                builder.background,
                builder.frame,
                builder.toast,
                builder.announce,
                builder.hide
        );

        advancement = mcBuilder.save(consumer, id.toString());
        ENTRIES_MAP.computeIfAbsent(builder.modid, $ -> new ArrayList<>()).add(this);
    }
    
    public void appendToLang(JsonObject object) {
        object.addProperty(titleKey(), title());
        object.addProperty(descriptionKey(), description());
    }

    public static JsonObject provideLangEntries(String modid) {
        JsonObject object = new JsonObject();
        var advancements = ENTRIES_MAP.get(modid);
        if(advancements==null) return object;
        for (var advancement : advancements) {
            advancement.appendToLang(object);
        }
        return object;
    }

    public static class Builder {
        private final String modid;
        @Nullable
        private final ResourceLocation background;
        private final String id;
        private final Advancement.Builder builder = Advancement.Builder.advancement();
        @Nullable
        private AdvancementHolder parent;
        private boolean builtin = true;
        private String title = "Untitled";
        private String description = "No Description";
        private ItemStack icon = ItemStack.EMPTY;
        private Function<HolderLookup.Provider, ItemStack> iconFunc = null;
        private AdvancementType frame = AdvancementType.TASK;
        private boolean toast = true;
        private boolean announce = false;
        private boolean hide = false;
        private final TriggerFactory factory;

        public Builder(String modid, String id, TriggerFactory factory) {
            this.modid = modid;
            this.id = id;
            this.background = "root".equals(id) ? ResourceLocation.fromNamespaceAndPath(modid,"textures/gui/advancements.png") : null;
            this.factory = factory;
        }
    
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder icon(ItemStack stack) {
            this.icon = stack;
            return this;
        }

        public Builder icon(ItemProviderEntry<?, ?> item) {
            return icon(item.asStack());
        }

        public Builder icon(ItemLike item) {
            return icon(new ItemStack(item));
        }

        public Builder icon(Function<HolderLookup.Provider, ItemStack> iconFunc) {
            this.iconFunc = iconFunc;
            return this;
        }

        public Builder frame(AdvancementType frame) {
            this.frame = frame;
            return this;
        }

        public Builder toast(boolean bl) {
            this.toast = bl;
            return this;
        }

        public Builder announce(boolean bl) {
            this.announce = bl;
            return this;
        }

        public Builder hidden() {
            this.hide = true;
            return this;
        }

        public Builder externalTrigger(String key, Criterion<?> trigger) {
            builder.addCriterion(key, trigger);
            this.builtin = false;
            return this;
        }

        public Builder parent(ResourceLocation id) {
            builder.parent(new net.minecraft.advancements.AdvancementHolder(id, new Advancement(Optional.empty(), Optional.empty(), AdvancementRewards.EMPTY, Map.of(), AdvancementRequirements.EMPTY,true, Optional.empty())));
            return this;
        }

        public Builder parent(AdvancementHolder advancement) {
            this.parent = advancement;
            return this;
        }

        public Builder transform(NonNullUnaryOperator<Advancement.Builder> transform) {
            transform.apply(builder);
            return this;
        }

        public AdvancementHolder build() {
            if (hide)
                description += "\u00A77\n(Hidden Advancement)";
            return new AdvancementHolder(this);
        }
        
    }
    
}
