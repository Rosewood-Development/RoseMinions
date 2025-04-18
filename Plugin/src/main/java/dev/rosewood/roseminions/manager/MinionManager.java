package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.config.Settings;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class MinionManager extends Manager {

    private final Set<Minion> loadedMinions;
    private final Set<ArmorStand> pendingLoadMinions;
    private BukkitTask minionTask, asyncMinionTask, loadPendingMinionsTask;

    public MinionManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.loadedMinions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.pendingLoadMinions = new HashSet<>();
    }

    public Collection<Minion> getLoadedMinions() {
        return Collections.unmodifiableCollection(this.loadedMinions);
    }

    public void destroyMinion(Minion minion) {
        this.loadedMinions.remove(minion);
        minion.getDisplayEntity().getPassengers().forEach(Entity::remove);
        minion.getDisplayEntity().remove();
    }

    public void registerMinion(Minion minion) {
        this.loadedMinions.add(minion);
    }

    public void queueMinionLoad(ArmorStand minionEntity) {
        this.pendingLoadMinions.add(minionEntity);
    }

    public void loadMinion(ArmorStand minionEntity) {
        try {
            Optional<Minion> existingMinion = this.getMinionFromEntity(minionEntity);
            if (existingMinion.isPresent())
                return;

            PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
            if (pdc.has(MinionUtils.MINION_DATA_KEY)) {
                if (!pdc.has(MinionUtils.MINION_DATA_KEY, PersistentDataType.TAG_CONTAINER)) {
                    minionEntity.remove();
                    RoseMinions.getInstance().getLogger().warning("Deleted minion entity with invalid PDC type at " + minionEntity.getLocation());
                    return;
                }
            }

            PersistentDataContainer dataContainer = pdc.get(MinionUtils.MINION_DATA_KEY, PersistentDataType.TAG_CONTAINER);
            if (dataContainer == null)
                return;

            Minion minion = new Minion(minionEntity, dataContainer);
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
        minion.unload();

        this.loadedMinions.remove(minion);

        PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
        PersistentDataAdapterContext context = pdc.getAdapterContext();

        PersistentDataContainer dataContainer = context.newPersistentDataContainer();
        minion.writePDC(dataContainer);
        pdc.set(MinionUtils.MINION_DATA_KEY, PersistentDataType.TAG_CONTAINER, dataContainer);
    }

    public Optional<Minion> getMinionFromEntity(ArmorStand entity) {
        return this.loadedMinions.stream()
                .filter(x -> x.getDisplayEntity().equals(entity))
                .findFirst();
    }

    @Override
    public void reload() {
        this.minionTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, () -> this.loadedMinions.forEach(Minion::update), 0L, Settings.MINION_UPDATE_FREQUENCY.get());
        this.asyncMinionTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin, () -> this.loadedMinions.forEach(Minion::updateAsync), 0L, Settings.MINION_UPDATE_FREQUENCY.get());
        this.loadPendingMinionsTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, () -> {
            this.pendingLoadMinions.stream().filter(ArmorStand::isValid).forEach(this::loadMinion);
            this.pendingLoadMinions.clear();
        }, 5L, 5L);

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

        if (this.loadPendingMinionsTask != null) {
            this.loadPendingMinionsTask.cancel();
            this.loadPendingMinionsTask = null;
        }

        // Kick out GUI viewers
        this.loadedMinions.forEach(Minion::kickOutViewers);

        // Unload minions that are currently loaded
        new ArrayList<>(this.loadedMinions).stream().map(Minion::getDisplayEntity).forEach(this::unloadMinion);

        this.pendingLoadMinions.clear();
    }

    public boolean isMinion(ArmorStand armorStand) {
        if (this.loadedMinions.stream().anyMatch(x -> x.getDisplayEntity().equals(armorStand)))
            return true;

        return armorStand.getPersistentDataContainer().has(MinionUtils.MINION_DATA_KEY);
    }

}
