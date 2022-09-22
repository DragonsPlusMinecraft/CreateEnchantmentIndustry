package com.simibubi.create.foundation.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.SimpleTrigger;

import java.util.StringJoiner;
import java.util.function.Consumer;

public abstract class AbstractEnchantmentIndustryAdvancement extends CreateAdvancement {
    protected final ResourceLocation id;
    protected final Advancement.Builder builder;
    @Nullable
    protected final SimpleTrigger builtinTrigger;
    protected final String titleKey;
    protected final String descriptionKey;
    
    public AbstractEnchantmentIndustryAdvancement(ResourceLocation id,
                                                  Advancement.Builder builder,
                                                  @Nullable SimpleTrigger builtinTrigger) {
        super("invalid", b -> b);
        this.id = id;
        this.builder = builder;
        this.builtinTrigger = builtinTrigger;
        if(builtinTrigger != null) this.builder.addCriterion("builtin", builtinTrigger.instance());
        this.titleKey = new StringJoiner(".").add("advancement").add(id.getNamespace()).add(id.getPath()).toString();
        this.descriptionKey = titleKey + ".desc";
        AllAdvancements.ENTRIES.remove(this);
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
    
    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        MinecraftServer server = sp.getServer();
        if (server == null)
            return true;
        Advancement advancement = server.getAdvancements().getAdvancement(this.id);
        if (advancement == null)
            return true;
        return sp.getAdvancements().getOrStartProgress(advancement).isDone();
    }
    
    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (this.builtinTrigger == null)
            throw new UnsupportedOperationException("Advancement [" + id + "] uses external triggers, it cannot be awarded directly");
        this.builtinTrigger.trigger(sp);
    }
    
    public abstract String title();
    
    public abstract String description();
    
    public void save(Consumer<Advancement> consumer) {
        datagenResult = builder.save(consumer, id.toString());
    }
    
    public void appendToLang(JsonObject object) {
        object.addProperty(titleKey(), title());
        object.addProperty(descriptionKey(), description());
    }
    
}
