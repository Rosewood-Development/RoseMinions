package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles all events that minion modules can listen to
 */
public class MinionModuleEventListener implements Listener {

    private final MinionManager minionManager;

    public MinionModuleEventListener(RosePlugin rosePlugin) {
        this.minionManager = rosePlugin.getManager(MinionManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        this.minionManager.handleEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        this.minionManager.handleEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.minionManager.handleEvent(event);
    }

}
