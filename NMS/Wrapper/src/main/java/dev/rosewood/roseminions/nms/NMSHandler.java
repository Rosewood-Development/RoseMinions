package dev.rosewood.roseminions.nms;

import dev.rosewood.roseminions.nms.hologram.Hologram;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows performing certain actions that are only possible through the use of NMS.
 * For internal use only. Subject to change extremely frequently.
 */
@ApiStatus.Internal
public interface NMSHandler {

    byte[] serializeItemStack(ItemStack itemStack);

    ItemStack deserializeItemStack(byte[] bytes);

    List<ItemStack> getFishingLoot(Entity looter, Location location, ItemStack fishingRod);

    void setPositionRotation(Entity entity, Location location);

    /**
     * Creates a hologram at the given location with the given text
     *
     * @param location The location to create the hologram at
     * @param text The text to display on the hologram
     * @return The hologram created
     */
    Hologram createHologram(Location location, List<String> text);

    /**
     * Sets the entity custom name bypassing the 256 character limit set by Bukkit.
     *
     * @param entity The entity to change the custom name of
     * @param customName The custom name to set
     */
    void setCustomNameUncapped(Entity entity, String customName);

}
