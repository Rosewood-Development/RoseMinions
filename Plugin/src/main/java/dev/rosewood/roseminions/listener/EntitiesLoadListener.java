package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

public class EntitiesLoadListener implements Listener {

    private final MinionManager minionManager;

    public EntitiesLoadListener(RosePlugin rosePlugin) {
        this.minionManager = rosePlugin.getManager(MinionManager.class);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        event.getEntities().stream()
                .filter(x -> x.getType() == EntityType.ARMOR_STAND)
                .map(x -> (ArmorStand) x)
                .forEach(this.minionManager::queueMinionLoad);
    }

    @EventHandler
    public void onEntitiesUnload(EntitiesUnloadEvent event) {
        event.getEntities().stream()
                .filter(x -> x.getType() == EntityType.ARMOR_STAND)
                .map(x -> (ArmorStand) x)
                .forEach(this.minionManager::unloadMinion);
    }

}
