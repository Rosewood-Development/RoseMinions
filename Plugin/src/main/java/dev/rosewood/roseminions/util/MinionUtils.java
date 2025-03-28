package dev.rosewood.roseminions.util;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingContainer;
import java.util.Random;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;

public final class MinionUtils {

    public static final NamespacedKey MINION_NEW_TYPE_KEY = CustomPersistentDataType.KeyHelper.get("minion_new_type");
    public static final NamespacedKey MINION_NEW_RANK_KEY = CustomPersistentDataType.KeyHelper.get("minion_new_rank");
    public static final NamespacedKey MINION_DATA_KEY = CustomPersistentDataType.KeyHelper.get("minion_data");
    public static final NamespacedKey MINION_NOTIFICATION_KEY = CustomPersistentDataType.KeyHelper.get("minion_notification");

    public static final String PRIMARY_COLOR = "<#c7a4ff>";
    public static final String SECONDARY_COLOR = "<#ffaaff>";

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

    /**
     * Snaps the inventory size to the nearest valid value (9, 18, 27, or intervals of 27)
     *
     * @param settings The settings container
     * @param sizeAccessor The setting accessor for the inventory size
     * @param inventoryAccessor The setting accessor for the inventory
     */
    public static void snapInventorySize(SettingContainer settings, SettingAccessor<Integer> sizeAccessor, SettingAccessor<ItemStack[]> inventoryAccessor) {
        int originalSize = settings.get(sizeAccessor);
        int inventorySize = originalSize;
        if (inventorySize % 9 != 0) {
            if (inventorySize < 9) {
                inventorySize = 9;
            } else if (inventorySize < 18) {
                inventorySize = 18;
            } else if (inventorySize < 27) {
                inventorySize = 27;
            }
        }

        if (inventorySize > 27)
            inventorySize = (int) Math.ceil(inventorySize / 27.0) * 27;

        if (inventorySize != originalSize)
            settings.set(sizeAccessor, inventorySize);

        // Adjust the inventory size if needed
        int size = settings.get(sizeAccessor);
        ItemStack[] contents = settings.get(inventoryAccessor);
        if (contents.length != size) {
            ItemStack[] newContents = new ItemStack[size];
            System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, size));
            settings.set(inventoryAccessor, newContents);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R forceCast(T value) {
        return (R) value;
    }

}
