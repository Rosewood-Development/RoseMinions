package dev.rosewood.roseminions.util;

import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;

@SuppressWarnings({"deprecation", "removal", "UnstableApiUsage"})
public class VersionUtils {

    public static final EntityType ITEM;
    public static final EntityType MOOSHROOM;
    public static final EntityType SNOW_GOLEM;
    public static final Particle DUST;
    public static final Particle POOF;
    public static final Particle SMOKE;
    public static final Particle BLOCK;
    public static final Particle BUBBLE;
    public static final Particle SPLASH;
    public static final Particle HAPPY_VILLAGER;
    public static final Enchantment INFINITY;
    public static final Enchantment SWEEPING_EDGE;
    public static final Enchantment LUCK_OF_THE_SEA;
    public static final ItemFlag HIDE_ADDITIONAL_TOOLTIP;
    public static final Attribute MAX_HEALTH;
    public static final Attribute KNOCKBACK_RESISTANCE;
    public static final Attribute LUCK_ATTRIBUTE;
    static {
        if (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 5)) {
            ITEM = EntityType.ITEM;
            MOOSHROOM = EntityType.MOOSHROOM;
            SNOW_GOLEM = EntityType.SNOW_GOLEM;
            POOF = Particle.POOF;
            SMOKE = Particle.SMOKE;
            DUST = Particle.DUST;
            BLOCK = Particle.BLOCK;
            BUBBLE = Particle.BUBBLE;
            SPLASH = Particle.SPLASH;
            HAPPY_VILLAGER = Particle.HAPPY_VILLAGER;
            INFINITY = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("infinity"));
            SWEEPING_EDGE = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sweeping_edge"));
            LUCK_OF_THE_SEA = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("luck_of_the_sea"));
            HIDE_ADDITIONAL_TOOLTIP = ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
        } else {
            ITEM = EntityType.valueOf("DROPPED_ITEM");
            MOOSHROOM = EntityType.valueOf("MUSHROOM_COW");
            SNOW_GOLEM = EntityType.valueOf("SNOWMAN");
            POOF = Particle.valueOf("EXPLOSION_NORMAL");
            SMOKE = Particle.valueOf("SMOKE_NORMAL");
            DUST = Particle.valueOf("REDSTONE");
            BLOCK = Particle.valueOf("BLOCK_CRACK");
            BUBBLE = Particle.valueOf("WATER_BUBBLE");
            SPLASH = Particle.valueOf("WATER_SPLASH");
            HAPPY_VILLAGER = Particle.valueOf("VILLAGER_HAPPY");
            INFINITY = findEnchantmentLegacy("infinity", "arrow_infinite");
            SWEEPING_EDGE = findEnchantmentLegacy("sweeping", "sweeping_edge");
            LUCK_OF_THE_SEA = findEnchantmentLegacy("luck", "luck_of_the_sea");
            HIDE_ADDITIONAL_TOOLTIP = ItemFlag.valueOf("HIDE_POTION_EFFECTS");
        }

        if (NMSUtil.getVersionNumber() > 21 || NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 3) {
            MAX_HEALTH = Attribute.MAX_HEALTH;
            KNOCKBACK_RESISTANCE = Attribute.KNOCKBACK_RESISTANCE;
            LUCK_ATTRIBUTE = Attribute.LUCK;
        } else {
            MAX_HEALTH = Attribute.valueOf("generic.max_health");
            KNOCKBACK_RESISTANCE = Attribute.valueOf("generic.knockback_resistance");
            LUCK_ATTRIBUTE = Attribute.valueOf("generic.luck");
        }
    }

    private static Enchantment findEnchantmentLegacy(String... names) {
        for (String name : names) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(name));
            if (enchantment != null)
                return enchantment;
        }
        return null;
    }

}
