package dev.rosewood.roseminions.util;

import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.rosegarden.utils.KeyHelper;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.hook.loot.Loot;
import dev.rosewood.roseminions.manager.HookProviderManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingContainer;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Panda;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

public final class MinionUtils {

    public static final NamespacedKey MINION_NEW_TYPE_KEY = KeyHelper.get("minion_new_type");
    public static final NamespacedKey MINION_NEW_RANK_KEY = KeyHelper.get("minion_new_rank");
    public static final NamespacedKey MINION_DATA_KEY = KeyHelper.get("minion_data");
    public static final NamespacedKey MINION_NOTIFICATION_KEY = KeyHelper.get("minion_notification");

    public static final String PRIMARY_COLOR = "<#c7a4ff>";
    public static final String SECONDARY_COLOR = "<#ffaaff>";

    public static final Random RANDOM = new Random();

    private MinionUtils() {

    }

    /**
     * Checks if a chance between 0-1 passes
     *
     * @param chance The chance
     * @return true if the chance passed, otherwise false
     */
    public static boolean checkChance(double chance) {
        return RANDOM.nextDouble() <= chance;
    }

    public static boolean isHostile(LivingEntity entity) {
        EntityType entityType = entity.getType();
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        if (entityClass == null)
            return false;

        if (NMSUtil.isPaper() && entity instanceof Mob mob && mob.isAggressive())
            return true;

        return switch (entityType.getKey().getKey()) {
            case "wolf" -> ((Wolf) entity).isAngry();
            case "bee" -> ((Bee) entity).getAnger() > 0;
            case "zombified_piglin", "zombie_pigman" -> ((PigZombie) entity).isAngry();
            case "rabbit" -> ((Rabbit) entity).getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY;
            case "panda" -> {
                Panda panda = ((Panda) entity);
                Panda.Gene main = panda.getMainGene();
                Panda.Gene hidden = panda.getHiddenGene();
                yield (main.isRecessive() ? (main == hidden ? main : Panda.Gene.NORMAL) : main) == Panda.Gene.AGGRESSIVE;
            }
            case "slime", "magma_cube" -> true;
            default -> Monster.class.isAssignableFrom(entityClass) || Boss.class.isAssignableFrom(entityClass);
        };
    }

    /**
     * Snaps the inventory size to the nearest valid value (9, 18, 27, or intervals of 27)
     *
     * @param settings The settings container
     * @param sizeSetting The setting for the inventory size
     * @param inventorySetting The setting for the inventory
     */
    public static void snapInventorySize(SettingContainer settings, PDCRoseSetting<Integer> sizeSetting, PDCRoseSetting<ItemStack[]> inventorySetting) {
        int originalSize = settings.get(sizeSetting);
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
            settings.set(sizeSetting, inventorySize);

        // Adjust the inventory size if needed
        int size = settings.get(sizeSetting);
        ItemStack[] contents = settings.get(inventorySetting);
        if (contents.length != size) {
            ItemStack[] newContents = new ItemStack[size];
            System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, size));
            settings.set(inventorySetting, newContents);
        }
    }

    public static Loot breakBlock(Minion minion, Block block, ItemStack tool) {
        List<ItemStack> items = new ArrayList<>(block.getDrops(tool));
        int experience = NMSAdapter.getHandler().getBlockExp(block, tool);
        Loot loot = new Loot(items, experience);
        return RoseMinions.getInstance().getManager(HookProviderManager.class).getLootProvider().destroy(loot, minion, block);
    }

}
