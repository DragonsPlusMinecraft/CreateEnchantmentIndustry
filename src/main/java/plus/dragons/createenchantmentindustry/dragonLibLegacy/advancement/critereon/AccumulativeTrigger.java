package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.antlr.v4.runtime.misc.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class AccumulativeTrigger extends AbstractTrigger<AccumulativeTrigger.TriggerInstance>{

    public AccumulativeTrigger(ResourceLocation pId) {
        super(pId);
    }

    public void trigger(Player pPlayer, int change){
        //this.trigger((ServerPlayer) pPlayer, (triggerInstance) -> triggerInstance.matches(id, pPlayer, change));
    }

    public Codec<TriggerInstance> codec() {
        return null; //TODO
    }

    private static class AccumulativeData extends SavedData {
        public Table<ResourceLocation,UUID,Integer> data;
        public void change(ResourceLocation resourceLocation ,UUID playerId, int i){
            var temp = data.get(resourceLocation,playerId);
            temp = temp==null? 0 : temp;
            temp +=i;
            data.put(resourceLocation,playerId,temp);
            setDirty();
        }
        public int get(ResourceLocation resourceLocation ,UUID playerId){
            var ret = data.get(resourceLocation, playerId);
            return ret == null ? 0 : ret;
        }
        public AccumulativeData() {
            data = HashBasedTable.create();
        }

        @SuppressWarnings("all")
        public static AccumulativeData load(CompoundTag compoundNBT, HolderLookup.Provider provider) {
            AccumulativeData ret = new AccumulativeData();

            if(!compoundNBT.contains("AccumulativeData"))
                return ret;

            var list = NBTHelper.readCompoundList((ListTag) compoundNBT.get("AccumulativeData"), c -> new TriCell(
                    NBTHelper.readResourceLocation(c,"TriggerId"),
                    c.getUUID("PlayerId"),
                    c.getInt("Count")
            ));

            list.forEach(triCell -> ret.data.put(triCell.rl,triCell.id,triCell.i));
            return ret;
        }

        @Override
        public CompoundTag save(CompoundTag pCompoundTag, HolderLookup.Provider pProvider) {
            var dataListTag = NBTHelper.writeCompoundList(data.cellSet().stream().toList(), cell -> {
                var ret = new CompoundTag();
                NBTHelper.writeResourceLocation(ret,"TriggerId",cell.getRowKey());
                ret.putUUID("PlayerId",cell.getColumnKey());
                ret.putInt("Count",cell.getValue());
                return ret;
            });
            pCompoundTag.put("AccumulativeData",dataListTag);
            return pCompoundTag;
        }

        private record TriCell(ResourceLocation rl, UUID id, int i){}
    }

    private static AccumulativeData get(Level level) {
        if (!(level instanceof ServerLevel)) {
            throw new RuntimeException("Attempted to get the data from a client world.");
        }

        ServerLevel serverWorld = level.getServer().overworld();
        DimensionDataStorage dimensionSavedDataManager = serverWorld.getDataStorage();
        return dimensionSavedDataManager.computeIfAbsent(new SavedData.Factory<>(AccumulativeData::new, AccumulativeData::load), "accumulative_data");
    }

    public static class TriggerInstance extends AbstractTrigger.Instance {
        private final MinMaxBounds.Ints requirement;

        public TriggerInstance(ResourceLocation pCriterion, ContextAwarePredicate player, MinMaxBounds.Ints requirement) {
//            super(pCriterion, player); FIXME
            this.requirement = requirement;
        }

        public boolean matches(ResourceLocation resourceLocation, Player player, int change) {
            AccumulativeData data = get(player.level());
            data.change(resourceLocation, player.getUUID(), change);
            return requirement.matches(data.get(resourceLocation, player.getUUID()));
        }

        protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
            return false;
        }

        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }

//        @Override
//        @NotNull
//        public JsonObject serializeToJson(@NotNull SerializationContext pConditions) {
//            JsonObject jsonObject = super.serializeToJson(pConditions);
//            jsonObject.add("requirement", requirement.serializeToJson());
//            return jsonObject;
//        }
    }
    
}
