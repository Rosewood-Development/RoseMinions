package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.ConfigurationManager.Setting;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class MinionManager extends Manager {

    private final List<Minion> loadedMinions;
    private BukkitTask minionTask, asyncMinionTask;

    public MinionManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.loadedMinions = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Minion> getLoadedMinions() {
        return Collections.unmodifiableList(this.loadedMinions);
    }

    public void destroyMinion(Minion minion) {
        this.loadedMinions.remove(minion);
        minion.getDisplayEntity().remove();
    }

    public void registerMinion(Minion minion) {
        this.loadedMinions.add(minion);
    }

    public void loadMinion(ArmorStand minionEntity) {
        try {
            Optional<Minion> existingMinion = this.getMinionFromEntity(minionEntity);
            if (existingMinion.isPresent())
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
        Optional<Minion> optionalMinion = this.getMinionFromEntity(minionEntity);
        if (optionalMinion.isEmpty())
            return;

        Minion minion = optionalMinion.get();
        this.loadedMinions.remove(minion);

        PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
        pdc.set(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY, minion.serialize());
    }

    public Optional<Minion> getMinionFromEntity(ArmorStand entity) {
        return this.loadedMinions.stream()
                .filter(x -> x.getDisplayEntity().equals(entity))
                .findFirst();
    }

    @Override
    public void reload() {
        this.minionTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, () -> this.loadedMinions.forEach(Minion::update), 0L, Setting.MINION_UPDATE_FREQUENCY.getLong());
        this.asyncMinionTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, () -> this.loadedMinions.forEach(Minion::updateAsync), 0L, Setting.MINION_UPDATE_FREQUENCY.getLong());

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

        if (this.asyncMinionTask != null) {
            this.asyncMinionTask.cancel();
            this.asyncMinionTask = null;
        }

        // Kick out GUI viewers
        this.loadedMinions.forEach(Minion::kickOutViewers);

        // Unload minions that are currently loaded
        new ArrayList<>(this.loadedMinions).stream().map(Minion::getDisplayEntity).forEach(this::unloadMinion);
    }

    public boolean isMinion(ArmorStand armorStand) {
        if (this.loadedMinions.stream().anyMatch(x -> x.getDisplayEntity().equals(armorStand)))
            return true;

        return armorStand.getPersistentDataContainer().has(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY);
    }

}
