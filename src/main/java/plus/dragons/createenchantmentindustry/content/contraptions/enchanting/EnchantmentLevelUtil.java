package plus.dragons.createenchantmentindustry.content.contraptions.enchanting;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class EnchantmentLevelUtil {

    private static final MethodHandle getMaxLevel;
    static {
        Method method;
        try {
            Class<?> EnchHooks = Class.forName(" dev.shadowsoffire.apotheosis.ench.asm.EnchHooks");
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
    public static int getMaxLevel(Enchantment enchantment){
        Integer maxLevel;
        try {
            maxLevel = (Integer) getMaxLevel.invoke(enchantment);
        } catch (Throwable throwable) {
            EnchantmentIndustry.LOGGER.warn("Failed to invoke getMaxLevel", throwable);
            maxLevel = enchantment.getMaxLevel();
        }
        return maxLevel;
    }
}
