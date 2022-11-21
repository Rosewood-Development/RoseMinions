package dev.rosewood.roseminions.nms;

import org.bukkit.inventory.ItemStack;

/**
 * Allows performing certain actions that are only possible through the use of NMS.
 * For internal use only. Subject to change extremely frequently.
 */
public interface NMSHandler {

    byte[] serializeItemStacks(ItemStack[] itemStacks);

    ItemStack[] deserializeItemStacks(byte[] bytes);

}
