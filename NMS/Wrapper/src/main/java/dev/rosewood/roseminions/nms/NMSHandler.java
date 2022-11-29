package dev.rosewood.roseminions.nms;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

/**
 * Allows performing certain actions that are only possible through the use of NMS.
 * For internal use only. Subject to change extremely frequently.
 */
public interface NMSHandler {

    byte[] serializeItemStack(ItemStack itemStack);

    ItemStack deserializeItemStack(byte[] bytes);

    default List<ItemStack> getFishingLoot(Entity looter, Location location, ItemStack fishingRod) {
        throw new UnsupportedOperationException("getFishingLoot is not supported on this version of Minecraft");
    }

}
