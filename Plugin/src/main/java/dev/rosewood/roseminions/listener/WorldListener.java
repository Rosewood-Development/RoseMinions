package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import java.util.Arrays;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListener implements Listener {

    private final MinionManager minionManager;

    public WorldListener(RosePlugin rosePlugin) {
        this.minionManager = rosePlugin.getManager(MinionManager.class);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Arrays.stream(event.getChunk().getEntities())
                .filter(x -> x.getType() == EntityType.ARMOR_STAND)
                .map(x -> (ArmorStand) x)
                .forEach(this.minionManager::loadMinion);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Arrays.stream(event.getChunk().getEntities())
                .filter(x -> x.getType() == EntityType.ARMOR_STAND)
                .map(x -> (ArmorStand) x)
                .forEach(this.minionManager::unloadMinion);
    }

}
