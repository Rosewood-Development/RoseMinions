package dev.rosewood.roseminions.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.ChunkLocation;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;

public final class MinionUtils {

    public static final NamespacedKey MINION_NEW_TYPE_KEY = new NamespacedKey(RoseMinions.getInstance(), "minion_new_type");
    public static final NamespacedKey MINION_NEW_RANK_KEY = new NamespacedKey(RoseMinions.getInstance(), "minion_new_rank");
    public static final NamespacedKey MINION_DATA_KEY = new NamespacedKey(RoseMinions.getInstance(), "minion_data");
    public static final NamespacedKey MINION_NOTIFICATION_KEY = new NamespacedKey(RoseMinions.getInstance(), "minion_notification");

    public static final String PRIMARY_COLOR = "<#c7a4ff>";
    public static final String SECONDARY_COLOR = "<#ffaaff>";

    public static final Random RANDOM = new Random();

    private static final Cache<ChunkLocation, ChunkSnapshot> chunkSnapshotCache = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .build();

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

    public static Material getLazyBlockMaterial(Location location) {
        World world = location.getWorld();
        if (world == null || location.getBlockY() < world.getMinHeight() || location.getBlockY() >= world.getMaxHeight())
            return Material.AIR;

        try {
            ChunkLocation pair = new ChunkLocation(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
            return chunkSnapshotCache.get(pair, () -> {
                Chunk chunk = location.getWorld().getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
                return chunk.getChunkSnapshot();
            }).getBlockType(location.getBlockX() & 15, location.getBlockY(), location.getBlockZ() & 15);
        } catch (Exception e) {
            RoseMinions.getInstance().getLogger().warning("Failed to fetch block type at " + location);
            e.printStackTrace();
            return Material.AIR;
        }
    }

    /**
     * Snaps the inventory size to the nearest valid value (9, 18, 27, or intervals of 27)
     *
     * @param settings The settings container
     * @param sizeAccessor The setting accessor for the inventory size
     * @param inventoryAccessor The setting accessor for the inventory
     */
    public static void snapInventorySize(SettingsContainer settings, SettingAccessor<Integer> sizeAccessor, SettingAccessor<ItemStack[]> inventoryAccessor) {
        int inventorySize = settings.get(sizeAccessor);
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

        if (inventorySize != settings.get(sizeAccessor))
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

}
