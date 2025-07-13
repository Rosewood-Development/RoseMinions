package dev.rosewood.roseminions.object;

import org.bukkit.entity.Player;

public interface GuiHolder {

    /**
     * Opens the GUI for the given player
     *
     * @param player the player
     */
    void openGui(Player player);

    /**
     * Kicks all viewers out of the GUI
     */
    void kickOutViewers();

}
