package dev.rosewood.roseminions.util;

import java.util.Random;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

public final class MinionUtils {

    public static final Random RANDOM = new Random();

    private MinionUtils() {

    }

    public static boolean isMonster(EntityType entityType) {
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        if (entityClass == null)
            return false;

        if (Monster.class.isAssignableFrom(entityClass) || Boss.class.isAssignableFrom(entityClass))
            return true;

        // Handle exceptions
        return switch (entityType) {
            case SLIME, MAGMA_CUBE -> true;
            default -> false;
        };
    }

}
