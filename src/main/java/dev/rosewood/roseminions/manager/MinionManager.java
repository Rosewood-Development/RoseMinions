package dev.rosewood.roseminions.manager;

import com.google.common.collect.Multimap;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.ConfigurationManager.Setting;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ChunkLocation;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class MinionManager extends Manager {

    private static final NamespacedKey MINION_DATA_KEY = new NamespacedKey(RoseMinions.getInstance(), "minion_data");

    private final DataManager dataManager;
    private final MinionTypeManager minionTypeManager;
    private Multimap<String, ChunkLocation> chunkLoadedMinionLocations;
    private List<Minion> loadedMinions;
    private BukkitTask minionTask;

    public MinionManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.dataManager = this.rosePlugin.getManager(DataManager.class);
        this.minionTypeManager = this.rosePlugin.getManager(MinionTypeManager.class);
        this.loadedMinions = Collections.synchronizedList(new ArrayList<>());
    }

    public void updateMinions() {
        this.loadedMinions.forEach(Minion::update);
    }

    public void registerMinion(Minion minion) {
        this.loadedMinions.add(minion);
    }

    public void loadMinion(ArmorStand minionEntity) {
        Minion existingMinion = this.getMinionFromEntity(minionEntity);
        if (existingMinion != null)
            return;

        PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
        if (!pdc.has(MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY))
            return;

        Minion minion = this.loadFromPDC(pdc);
        if (minion != null)
            this.loadedMinions.add(minion);
    }

    public void unloadMinion(ArmorStand minionEntity) {
        Minion minion = this.getMinionFromEntity(minionEntity);
        if (minion == null)
            return;

        this.loadedMinions.remove(minion);

        minionEntity.remove(); // TODO: REMOVE THIS

//        PersistentDataContainer pdc = minionEntity.getPersistentDataContainer();
//        minion.save(pdc);
    }

    private Minion getMinionFromEntity(ArmorStand entity) {
        return this.loadedMinions.stream()
                .filter(x -> x.getDisplayEntity().equals(entity))
                .findFirst()
                .orElse(null);
    }

    public void saveToPDC(Minion minion, PersistentDataContainer pdc) {

    }

    public Minion loadFromPDC(PersistentDataContainer pdc) {
        byte[] data = pdc.get(MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY);
        if (data == null)
            return null;

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(new GZIPInputStream(inputStream))) {

            int dataVersion = dataInput.readInt();
            if (dataVersion == 1) {
                String minionType = dataInput.readUTF();
                int rankLevel = dataInput.readInt();

                int length = dataInput.readInt();
                byte[] minionData = new byte[length];
                for (int i = 0; i < length; i++)
                    minionData[i] = dataInput.readByte();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reload() {
        this.chunkLoadedMinionLocations = this.dataManager.getChunkLoadedMinions();

        this.minionTask = Bukkit.getScheduler().runTaskTimer(this.rosePlugin, this::updateMinions, 0L, Setting.MINION_UPDATE_FREQUENCY.getLong());

        // Load minions from chunks that are already loaded
//        for (World world : Bukkit.getWorlds())
//            for (ArmorStand minionEntity : world.getEntitiesByClass(ArmorStand.class))
//                this.loadMinion(minionEntity);
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

}
