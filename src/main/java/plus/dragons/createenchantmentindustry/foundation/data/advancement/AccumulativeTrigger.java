package plus.dragons.createenchantmentindustry.foundation.data.advancement;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.advancements.critereon.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class AccumulativeTrigger extends SimpleCriterionTrigger<AccumulativeTrigger.TriggerInstance>{

    final ResourceLocation id;

    public AccumulativeTrigger(ResourceLocation pId) {
        this.id = pId;
    }

    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject pJson, @NotNull EntityPredicate.Composite pPlayer, @NotNull DeserializationContext pContext) {
        MinMaxBounds.Ints requirements = MinMaxBounds.Ints.fromJson(pJson.get("requirement"));
        return new TriggerInstance(id,pPlayer,requirements);
    }

    public void trigger(Player pPlayer, int change){
        this.trigger((ServerPlayer) pPlayer, (triggerInstance) -> triggerInstance.matches(id,pPlayer,change));
    }

    @Override
    @NotNull
    public ResourceLocation getId() {
        return id;
    }

    private static class AccumulativeData extends SavedData {
        private final Gson gson;
        private final Type type;

        public Table<ResourceLocation,UUID,Integer> data;

        public void change(ResourceLocation resourceLocation ,UUID playerId, int i){
            var temp = data.get(resourceLocation,playerId);
            temp = temp==null? 0 : temp;
            temp +=i;
            data.put(resourceLocation,playerId,temp);
            setDirty();
        }

        public int get(ResourceLocation resourceLocation ,UUID playerId){
            var ret = data.get(resourceLocation,playerId);
            return ret==null?0:ret;
        }

        public AccumulativeData() {
            gson = new Gson();
            type = new TypeToken<Map<ResourceLocation,Map<UUID,Integer>>>() {}.getType();
            data = HashBasedTable.create();
        }

        // TODO serialization hasn't been tested yet
        @SuppressWarnings("unchecked")
        public static AccumulativeData load(CompoundTag compoundNBT){
            AccumulativeData ret = new AccumulativeData();
            if(compoundNBT.contains("accumulative_data")){
                var rowMap = (Map<ResourceLocation,Map<UUID,Integer>>) ret.gson.fromJson(compoundNBT.getString("accumulative_data"),ret.type);
                for(var rl:rowMap.keySet()){
                    for(var entry:rowMap.get(rl).entrySet())
                        ret.data.put(rl,entry.getKey(),entry.getValue());
                }
            }
            return ret;
        }

        @Override
        public CompoundTag save(CompoundTag pCompoundTag) {
            pCompoundTag.putString("accumulative_data",gson.toJson(data.rowMap(),type));
            return pCompoundTag;
        }
    }

    private static AccumulativeData get(Level level){
        if (!(level instanceof ServerLevel)) {
            throw new RuntimeException("Attempted to get the data from a client world.");
        }

        ServerLevel serverWorld = level.getServer().overworld();
        DimensionDataStorage dimensionSavedDataManager = serverWorld.getDataStorage();
        return dimensionSavedDataManager.computeIfAbsent(AccumulativeData::load,AccumulativeData::new, "accumulative_data");
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints requirement;

        public static AccumulativeTrigger.TriggerInstance ofBook(int requirement){
            return new AccumulativeTrigger.TriggerInstance(ModTriggers.BOOK_PRINTED.id, EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(requirement));
        }

        public static AccumulativeTrigger.TriggerInstance ofExperienceDisenchanted(int requirement){
            return new AccumulativeTrigger.TriggerInstance(ModTriggers.DISENCHANTED.id, EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(requirement));
        }

        public TriggerInstance(ResourceLocation pCriterion, EntityPredicate.Composite pPlayer, MinMaxBounds.Ints requirement) {
            super(pCriterion, pPlayer);
            this.requirement = requirement;
        }

        public boolean matches(ResourceLocation resourceLocation,Player player,int change) {
            AccumulativeData data = get(player.level);
            data.change(resourceLocation,player.getUUID(),change);
            return requirement.matches(data.get(resourceLocation,player.getUUID()));
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(@NotNull SerializationContext pConditions) {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("requirement", requirement.serializeToJson());
            return jsonobject;
        }
    }
}
