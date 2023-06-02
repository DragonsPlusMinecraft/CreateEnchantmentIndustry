package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;

public class EnchantmentEntry extends Pair<Enchantment, Integer> {
    public static final TagKey<Enchantment> HYPER_ENCHANTABLE =
        TagKey.create(Registry.ENCHANTMENT_REGISTRY, EnchantmentIndustry.genRL("hyper_enchantable"));
    public static final TagKey<Enchantment> HYPER_ENCHANTABLE_BLACKLIST =
            TagKey.create(Registry.ENCHANTMENT_REGISTRY, EnchantmentIndustry.genRL("hyper_enchantable_blacklist"));
    private static final MethodHandle getMaxLevel;
    static {
        Method method;
        try {
            Class<?> EnchHooks = Class.forName("shadows.apotheosis.ench.asm.EnchHooks");
            method = EnchHooks.getMethod("getMaxLevel", Enchantment.class);
        } catch (Throwable exception) {
            EnchantmentIndustry.LOGGER.debug("Failed to load EnchHooks from Apotheosis, fall back to vanilla method...");
            method = ObfuscationReflectionHelper.findMethod(Enchantment.class, "m_6586_");
        }
        try {
            method.setAccessible(true);
            getMaxLevel = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Failed to access Enchantment#getMaxLevel!");
        }
    }
    
    protected EnchantmentEntry(Enchantment first, Integer second) {
        super(first, second);
    }

    public static EnchantmentEntry of(Enchantment enchantment, Integer level) {
        return new EnchantmentEntry(enchantment, level);
    }

    public static EnchantmentEntry of(Enchantment enchantment, int level) {
        return new EnchantmentEntry(enchantment, level);
    }

    public boolean valid() {
        var enchantment = getFirst();
        int level = getSecond();
        int maxLevel;

        try {
            maxLevel = (Integer) getMaxLevel.invoke(enchantment);
        } catch (Throwable throwable) {
            EnchantmentIndustry.LOGGER.warn("Failed to invoke getMaxLevel", throwable);
            maxLevel = enchantment.getMaxLevel();
        }

        Optional<Holder<Enchantment>> optional = ForgeRegistries.ENCHANTMENTS.getHolder(enchantment);
        if (optional.isPresent()) {
            Holder<Enchantment> holder = optional.get();
            if (holder.is(HYPER_ENCHANTABLE_BLACKLIST)) {
                return level <= maxLevel;
            } else if (maxLevel == 1 && level > 1) {
                return holder.is(HYPER_ENCHANTABLE) && CeiConfigs.SERVER.enableHyperEnchant.get() && level <= maxLevel + CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get();
            }
        }
        return level <= maxLevel + (CeiConfigs.SERVER.enableHyperEnchant.get() ? CeiConfigs.SERVER.maxHyperEnchantingLevelExtension.get() : 0);
    }
}
