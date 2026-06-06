package plus.dragons.createenchantmentindustry.content.contraptions.enchanting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class EnchantmentLevelCapUtil {
    private static List<? extends String> cachedEntries = List.of();
    private static Map<ResourceLocation, Integer> cachedCaps = Map.of();

    public static OptionalInt getConfiguredCap(Enchantment enchantment) {
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null || CeiConfigs.SERVER.enchantmentLevelCaps == null)
            return OptionalInt.empty();

        Map<ResourceLocation, Integer> caps = parseCaps();
        Integer cap = caps.get(id);
        return cap == null ? OptionalInt.empty() : OptionalInt.of(cap);
    }

    public static int getAllowedMaxLevel(Enchantment enchantment) {
        return getConfiguredCap(enchantment)
                .orElseGet(() -> EnchantmentLevelUtil.getMaxLevel(enchantment)
                        + (CeiConfigs.SERVER.enableHyperEnchant.get()
                        ? CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get()
                        : 0));
    }

    public static boolean exceedsConfiguredCap(Enchantment enchantment, int level) {
        OptionalInt cap = getConfiguredCap(enchantment);
        return cap.isPresent() && level > cap.getAsInt();
    }

    private static synchronized Map<ResourceLocation, Integer> parseCaps() {
        List<? extends String> entries = List.copyOf(CeiConfigs.SERVER.enchantmentLevelCaps.get());
        if (entries.equals(cachedEntries))
            return cachedCaps;

        Map<ResourceLocation, Integer> caps = new HashMap<>();
        for (String raw : entries) {
            String entry = raw.trim();
            if (entry.isEmpty())
                continue;
            int split = entry.lastIndexOf('=');
            if (split <= 0 || split == entry.length() - 1) {
                EnchantmentIndustry.LOGGER.warn("Ignoring invalid enchantment level cap entry '{}'. Expected modid:enchantment=level", raw);
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry.substring(0, split).trim());
            if (id == null) {
                EnchantmentIndustry.LOGGER.warn("Ignoring invalid enchantment id in level cap entry '{}'", raw);
                continue;
            }
            try {
                int cap = Integer.parseInt(entry.substring(split + 1).trim());
                if (cap < 0) {
                    EnchantmentIndustry.LOGGER.warn("Ignoring negative enchantment level cap entry '{}'", raw);
                    continue;
                }
                caps.put(id, cap);
            } catch (NumberFormatException exception) {
                EnchantmentIndustry.LOGGER.warn("Ignoring invalid enchantment level cap entry '{}'", raw);
            }
        }
        cachedEntries = entries;
        cachedCaps = Map.copyOf(caps);
        return cachedCaps;
    }
}
