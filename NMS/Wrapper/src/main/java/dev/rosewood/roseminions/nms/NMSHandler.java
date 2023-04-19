package dev.rosewood.roseminions.nms;

import java.util.List;
import dev.rosewood.roseminions.nms.hologram.Hologram;
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

    void setPositionRotation(Entity entity, Location location);

    /**
     * Creates a hologram at the given location with the given text
     *
     * @param location The location to create the hologram at
     * @param text The text to display on the hologram
     * @return The hologram created
     */
    Hologram createHologram(Location location, List<String> text);

}
