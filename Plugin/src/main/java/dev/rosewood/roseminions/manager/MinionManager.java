package dev.rosewood.roseminions.manager;

import com.google.common.collect.Multimap;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.ConfigurationManager.Setting;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ChunkLocation;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class MinionManager extends Manager {

    private final DataManager dataManager;
    private final List<Minion> loadedMinions;
    private Multimap<String, ChunkLocation> chunkLoadedMinionLocations;
    private BukkitTask minionTask;

    public MinionManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.dataManager = this.rosePlugin.getManager(DataManager.class);
        this.loadedMinions = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Minion> getLoadedMinions() {
        return Collections.unmodifiableList(this.loadedMinions);
    }

    public void destroyMinion(Minion minion) {
        this.loadedMinions.remove(minion);
        minion.getDisplayEntity().remove();
    }

    public void updateMinions() {
        this.loadedMinions.forEach(Minion::update);
    }

    public void registerMinion(Minion minion) {
        this.loadedMinions.add(minion);
    }

    public void loadMinion(ArmorStand minionEntity) {
        try {
            Minion existingMinion = this.getMinionFromEntity(minionEntity);
            if (existingMinion != null)
                return;

            PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
            byte[] data = pdc.get(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY);
            if (data == null)
                return;

            Minion minion = new Minion(minionEntity, data);
            this.loadedMinions.add(minion);
        } catch (Exception e) {
            RoseMinions.getInstance().getLogger().warning("Failed to load minion from entity " + minionEntity.getUniqueId());
            e.printStackTrace();
        }
    }

    public void unloadMinion(ArmorStand minionEntity) {
        Minion minion = this.getMinionFromEntity(minionEntity);
        if (minion == null)
            return;

        this.loadedMinions.remove(minion);

        PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
        pdc.set(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY, minion.serialize());
    }

    public Minion getMinionFromEntity(ArmorStand entity) {
        return this.loadedMinions.stream()
                .filter(x -> x.getDisplayEntity().equals(entity))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void reload() {
        this.chunkLoadedMinionLocations = this.dataManager.getChunkLoadedMinions();

        this.minionTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, this::updateMinions, 0L, Setting.MINION_UPDATE_FREQUENCY.getLong());

        // Load minions from chunks that are already loaded
        for (World world : Bukkit.getWorlds())
            for (ArmorStand minionEntity : world.getEntitiesByClass(ArmorStand.class))
                this.loadMinion(minionEntity);
    }

    @Override
    public void disable() {
        if (this.minionTask != null) {
            this.minionTask.cancel();
            this.minionTask = null;
        }

        this.chunkLoadedMinionLocations.clear();

        // Unload minions that are currently loaded
        new ArrayList<>(this.loadedMinions).stream().map(Minion::getDisplayEntity).forEach(this::unloadMinion);
    }

    public boolean isMinion(ArmorStand armorStand) {
        return this.loadedMinions.stream().anyMatch(x -> x.getDisplayEntity().equals(armorStand));
    }

}
