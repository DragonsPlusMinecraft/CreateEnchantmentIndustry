package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.SimpleTrigger;
import plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon.TriggerFactory;
import plus.dragons.createenchantmentindustry.foundation.mixin.dragonLibLegacy.CreateAdvancementConstructor;

public class AdvancementHolder {
    public static final Map<String, List<AdvancementHolder>> ENTRIES_MAP = new HashMap<>();
    protected final ResourceLocation id;
    protected final Advancement.Builder builder;
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
    protected Advancement advancement;

    protected AdvancementHolder(String modid, String id, Advancement.Builder builder, @Nullable AdvancementHolder parent, boolean builtin, String title, String description, TriggerFactory triggerFactory) {
        this.id = new ResourceLocation(modid, id);
        this.builder = builder;
        this.parent = parent;
        if (builtin) {
            this.builtinTrigger = triggerFactory.simple(new ResourceLocation(modid, "builtin/" + id));
            this.builder.addCriterion("builtin", builtinTrigger.instance());
        } else this.builtinTrigger = null;
        this.createAdvancement = CreateAdvancementConstructor.createInstance(id, $ -> $);
        ((CreateAdvancementAccess) createAdvancement).fromAdvancementHolder(this);
        this.titleKey = new StringJoiner(".").add("advancement").add(modid).add(id).toString();
        this.descriptionKey = titleKey + ".desc";
        this.title = title;
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
        if (createAdvancement == null)
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
        if (parent != null) builder.parent(parent.advancement);
        advancement = builder.save(consumer, id.toString());
    }

    public void appendToLang(JsonObject object) {
        object.addProperty(titleKey(), title());
        object.addProperty(descriptionKey(), description());
    }

    public static JsonObject provideLangEntries(String modid) {
        JsonObject object = new JsonObject();
        var advancements = ENTRIES_MAP.get(modid);
        if (advancements == null) return object;
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
        private FrameType frame = FrameType.TASK;
        private boolean toast = true;
        private boolean announce = false;
        private boolean hide = false;
        private final TriggerFactory factory;

        public Builder(String modid, String id, TriggerFactory factory) {
            this.modid = modid;
            this.id = id;
            this.background = "root".equals(id) ? new ResourceLocation(modid, "textures/gui/advancements.png") : null;
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

        public Builder icon(ItemProviderEntry<?> item) {
            return icon(item.asStack());
        }

        public Builder icon(ItemLike item) {
            return icon(new ItemStack(item));
        }

        public Builder frame(FrameType frame) {
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

        public Builder externalTrigger(String key, CriterionTriggerInstance trigger) {
            builder.addCriterion(key, trigger);
            this.builtin = false;
            return this;
        }

        public Builder parent(ResourceLocation id) {
            builder.parent(new Advancement(id, null, null, AdvancementRewards.EMPTY, Map.of(), new String[0][0], true));
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
            AdvancementHolder advancement = new AdvancementHolder(modid, id, builder, parent, builtin, title, description, factory);
            builder.display(
                    icon,
                    Component.translatable(advancement.titleKey),
                    Component.translatable(advancement.descriptionKey).withStyle(s -> s.withColor(0xDBA213)),
                    background,
                    frame,
                    toast,
                    announce,
                    hide);
            ENTRIES_MAP.computeIfAbsent(modid, $ -> new ArrayList<>()).add(advancement);
            return advancement;
        }
    }
}
