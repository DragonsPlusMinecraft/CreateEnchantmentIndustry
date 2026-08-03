package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import plus.dragons.createenchantmentindustry.compat.clumps.ClumpsCompat;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.foundation.mixin.ExperienceOrbAccessor;

public class RawExperienceUtil {
    public static final String RAW_XP_ORB_TAG = "CreateEnchantmentIndustryRawXp";

    public static void markRawExperienceOrb(ExperienceOrb orb) {
        orb.getPersistentData().putBoolean(RAW_XP_ORB_TAG, true);
    }

    public static boolean isRawExperienceOrb(ExperienceOrb orb) {
        return orb.getPersistentData().getBoolean(RAW_XP_ORB_TAG);
    }

    public static void addRawExperience(Player player, int amount) {
        if (amount <= 0)
            return;
        setRawExperience(player, getPlayerExperience(player) + amount);
    }

    public static int removeRawExperience(Player player, int amount) {
        if (amount <= 0)
            return 0;
        int current = getPlayerExperience(player);
        int removed = Math.min(current, amount);
        if (removed > 0)
            setRawExperience(player, current - removed);
        return removed;
    }

    public static void pickupRawExperience(ExperienceOrb orb, Player player, @Nullable IntConsumer additionalEffects) {
        if (orb.level().isClientSide || player.takeXpDelay != 0)
            return;
        OrbExperienceGroups groups = getOrbExperienceGroups(orb);
        if (groups.total() <= 0 || MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb)))
            return;

        player.takeXpDelay = 2;
        player.take(orb, 1);
        int left = 0;
        for (var entry : groups.values().entrySet()) {
            for (int count = 0; count < entry.getValue(); count++) {
                left += repairPlayerItems(orb, player, entry.getKey());
            }
        }
        if (left > 0) {
            addRawExperience(player, left);
            if (additionalEffects != null)
                additionalEffects.accept(left);
        }
        orb.discard();
    }

    public static int getOrbExperience(ExperienceOrb orb) {
        return getOrbExperienceGroups(orb).total();
    }

    public static int removeExperienceFromOrb(ExperienceOrb orb, int amount) {
        if (amount <= 0 || orb.isRemoved())
            return 0;
        OrbExperienceGroups groups = getOrbExperienceGroups(orb);
        int target = Math.min(amount, groups.total());
        if (target <= 0)
            return 0;
        if (target == groups.total()) {
            orb.discard();
            return target;
        }

        RemovalResult result = groups.clumped()
                ? removeFromClumpedGroups(groups.values(), target)
                : removeFromVanillaGroup(groups.values(), target);
        if (result.removed() <= 0)
            return 0;
        if (groups.clumped()) {
            if (!ClumpsCompat.setExperienceGroups(orb, result.remaining()))
                return 0;
            setOrbCount(orb, countGroups(result.remaining()));
        } else {
            applyVanillaGroups(orb, result.remaining());
        }
        return result.removed();
    }

    public static int getPlayerExperience(Player player) {
        if (player.experienceLevel == 0 && player.experienceProgress == 0)
            return 0;
        int total = Enchanting.expPointFromLevel(player.experienceLevel);
        int bar = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return Math.max(total + bar, 1);
    }

    private static void setRawExperience(Player player, int total) {
        total = Math.max(0, total);
        int level = getLevelForExperience(total);
        int levelStart = Enchanting.expPointFromLevel(level);
        int pointsIntoLevel = total - levelStart;
        int needed = Enchanting.expPointForNextLevel(level);

        player.totalExperience = total;
        player.experienceLevel = level;
        player.experienceProgress = needed == 0 ? 0 : (float) pointsIntoLevel / needed;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetExperiencePacket(
                    serverPlayer.experienceProgress,
                    serverPlayer.totalExperience,
                    serverPlayer.experienceLevel));
        }
    }

    private static int getLevelForExperience(int total) {
        int high = 1;
        while (Enchanting.expPointFromLevel(high) <= total && high < 21863)
            high *= 2;
        int low = high / 2;
        while (low + 1 < high) {
            int mid = low + (high - low) / 2;
            if (Enchanting.expPointFromLevel(mid) <= total)
                low = mid;
            else
                high = mid;
        }
        return low;
    }

    public static int repairPlayerItems(ExperienceOrb orb, Player player, int value) {
        return ((ExperienceOrbAccessor) orb).create_enchantment_industry$repairPlayerItems(player, value);
    }

    private static OrbExperienceGroups getOrbExperienceGroups(ExperienceOrb orb) {
        boolean clumpsLoaded = ClumpsCompat.isLoaded();
        Map<Integer, Integer> clumped = ClumpsCompat.getExperienceGroups(orb);
        if (clumped != null)
            return new OrbExperienceGroups(clumped, true);
        int count = clumpsLoaded ? 1 : Math.max(getOrbCount(orb), 1);
        Map<Integer, Integer> vanilla = new LinkedHashMap<>();
        if (orb.value > 0)
            vanilla.put(orb.value, count);
        return new OrbExperienceGroups(vanilla, clumpsLoaded);
    }

    private static RemovalResult removeFromClumpedGroups(Map<Integer, Integer> groups, int amount) {
        Map<Integer, Integer> remaining = new LinkedHashMap<>();
        int toRemove = amount;
        for (var entry : groups.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();
            if (toRemove > 0) {
                int whole = Math.min(count, toRemove / value);
                count -= whole;
                toRemove -= whole * value;
                if (toRemove > 0 && count > 0) {
                    int partial = value - toRemove;
                    count--;
                    toRemove = 0;
                    if (partial > 0)
                        remaining.merge(partial, 1, Integer::sum);
                }
            }
            if (count > 0)
                remaining.merge(value, count, Integer::sum);
        }
        return new RemovalResult(remaining, amount - toRemove);
    }

    private static RemovalResult removeFromVanillaGroup(Map<Integer, Integer> groups, int amount) {
        if (groups.isEmpty())
            return new RemovalResult(Map.of(), 0);
        var entry = groups.entrySet().iterator().next();
        int value = entry.getKey();
        int count = entry.getValue();
        if (count == 1) {
            int removed = Math.min(value, amount);
            int remainingValue = value - removed;
            return new RemovalResult(remainingValue > 0 ? Map.of(remainingValue, 1) : Map.of(), removed);
        }
        int removedCount = Math.min(count, amount / value);
        int remainingCount = count - removedCount;
        return new RemovalResult(remainingCount > 0 ? Map.of(value, remainingCount) : Map.of(), removedCount * value);
    }

    private static void applyVanillaGroups(ExperienceOrb orb, Map<Integer, Integer> groups) {
        if (groups.isEmpty()) {
            orb.discard();
            return;
        }
        var entry = groups.entrySet().iterator().next();
        orb.value = entry.getKey();
        setOrbCount(orb, entry.getValue());
    }

    private static int getOrbCount(ExperienceOrb orb) {
        return ((ExperienceOrbAccessor) orb).create_enchantment_industry$getCount();
    }

    private static void setOrbCount(ExperienceOrb orb, int count) {
        ((ExperienceOrbAccessor) orb).create_enchantment_industry$setCount(count);
    }

    private static int countGroups(Map<Integer, Integer> groups) {
        long count = 0;
        for (int value : groups.values()) {
            count += value;
            if (count >= Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    private record OrbExperienceGroups(Map<Integer, Integer> values, boolean clumped) {
        int total() {
            long total = 0;
            for (var entry : values.entrySet()) {
                total += (long) entry.getKey() * entry.getValue();
                if (total >= Integer.MAX_VALUE)
                    return Integer.MAX_VALUE;
            }
            return (int) total;
        }
    }

    private record RemovalResult(Map<Integer, Integer> remaining, int removed) {}
}
