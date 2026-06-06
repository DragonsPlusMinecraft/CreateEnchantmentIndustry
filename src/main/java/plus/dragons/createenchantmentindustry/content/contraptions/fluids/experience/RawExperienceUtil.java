package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;

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

    public static int getPlayerExperience(Player player) {
        if (player.experienceLevel == 0 && player.experienceProgress == 0)
            return 0;
        int total = Enchanting.expPointFromLevel(player.experienceLevel);
        int bar = (int) (player.experienceProgress * player.getXpNeededForNextLevel());
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
        return orb.repairPlayerItems(player, value);
    }
}
