package plus.dragons.createenchantmentindustry.compat.clumps;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.fml.ModList;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public final class ClumpsCompat {
    private static final String MOD_ID = "clumps";
    private static final String CLUMPED_ORB_CLASS = "com.blamejared.clumps.helper.IClumpedOrb";

    private static volatile boolean initialized;
    private static boolean warned;
    private static Class<?> clumpedOrbClass;
    private static Method getClumpedMap;
    private static Method setClumpedMap;

    private ClumpsCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    @Nullable
    public static Map<Integer, Integer> getExperienceGroups(ExperienceOrb orb) {
        if (!isLoaded())
            return null;
        initialize();
        if (clumpedOrbClass == null || getClumpedMap == null || !clumpedOrbClass.isInstance(orb))
            return null;
        try {
            Object result = getClumpedMap.invoke(orb);
            if (!(result instanceof Map<?, ?> map))
                return null;
            Map<Integer, Integer> groups = new LinkedHashMap<>();
            for (var entry : map.entrySet()) {
                if (!(entry.getKey() instanceof Number value) || !(entry.getValue() instanceof Number count))
                    continue;
                int intValue = value.intValue();
                int intCount = count.intValue();
                if (intValue > 0 && intCount > 0)
                    groups.merge(intValue, intCount, Integer::sum);
            }
            return groups.isEmpty() ? null : groups;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warn(exception);
            return null;
        }
    }

    public static boolean setExperienceGroups(ExperienceOrb orb, Map<Integer, Integer> groups) {
        if (!isLoaded())
            return false;
        initialize();
        if (clumpedOrbClass == null || setClumpedMap == null || !clumpedOrbClass.isInstance(orb))
            return false;
        try {
            setClumpedMap.invoke(orb, new LinkedHashMap<>(groups));
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warn(exception);
            return false;
        }
    }

    private static void initialize() {
        if (initialized)
            return;
        synchronized (ClumpsCompat.class) {
            if (initialized)
                return;
            initialized = true;
            try {
                clumpedOrbClass = Class.forName(CLUMPED_ORB_CLASS);
                getClumpedMap = clumpedOrbClass.getMethod("clumps$getClumpedMap");
                setClumpedMap = clumpedOrbClass.getMethod("clumps$setClumpedMap", Map.class);
            } catch (ReflectiveOperationException | LinkageError exception) {
                warn(exception);
            }
        }
    }

    private static void warn(Throwable throwable) {
        if (warned)
            return;
        warned = true;
        EnchantmentIndustry.LOGGER.warn("Failed to access Clumps experience data; raw experience clumps will not be partially drained", throwable);
    }
}
