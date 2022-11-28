package dev.rosewood.roseminions.minion;

import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.controller.AnimationController;
import dev.rosewood.roseminions.minion.gui.MinionGui;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.model.DataSerializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class Minion implements DataSerializable {

    private MinionData minionData;
    private int rank;

    private Reference<ArmorStand> displayEntity;
    private UUID owner;
    private Location location;
    private boolean chunkLoaded;
    private final Map<Class<? extends MinionModule>, MinionModule> modules;

    private final AnimationController animationController;
    private final MinionGui gui;

    private Minion(ArmorStand displayEntity) {
        this.displayEntity = new WeakReference<>(displayEntity);
        this.modules = new LinkedHashMap<>();

        this.animationController = new AnimationController(this);
        this.gui = new MinionGui(this);
    }

    /**
     * Used for loading a minion from an existing entity in the world
     */
    public Minion(ArmorStand displayEntity, byte[] data) {
        this(displayEntity);
        this.deserialize(data);
    }

    /**
     * Used for placing a minion from an item
     */
    public Minion(Location location, byte[] data) {
        this(null);
        this.displayEntity = new WeakReference<>(null);
        this.location = location;
        this.deserialize(data);
    }

    /**
     * Used for creating a new minion
     */
    public Minion(MinionData minionData, int rank, UUID owner, Location location, boolean chunkLoaded) {
        this(null);

        this.minionData = minionData;
        this.rank = rank;
        this.owner = owner;
        this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.chunkLoaded = chunkLoaded;
        this.loadRankData();
    }

    private void loadRankData() {
        MinionModuleManager minionModuleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);
        MinionData.MinionRank rank = this.minionData.getRank(this.rank);
        this.modules.putAll(rank.getModuleSettings().entrySet().stream().map(entry -> {
            MinionModule module = minionModuleManager.createModule(entry.getKey(), this);
            if (module == null)
                throw new IllegalStateException("Failed to create module " + entry.getKey() + "!");
            module.mergeSettings(entry.getValue());
            return module;
        }).collect(Collectors.toMap(MinionModule::getClass, Function.identity())));
        this.animationController.mergeSettings(rank.getAnimationSettings());
    }

    @SuppressWarnings("unchecked")
    public <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass) {
        return Optional.ofNullable((T) this.modules.get(moduleClass));
    }

    public List<MinionModule> getModules() {
        return List.copyOf(this.modules.values());
    }

    public void update() {
        ArmorStand displayEntity = this.displayEntity.get();
        if (displayEntity == null || !displayEntity.isValid()) {
            this.displayEntity = new WeakReference<>(this.createDisplayEntity());
            this.animationController.updateEntity();
        }

        this.animationController.update();
        this.modules.values().forEach(MinionModule::update);
    }

    public void updateAsync() {
        this.animationController.updateAsync();
        this.modules.values().forEach(MinionModule::updateAsync);
    }

    public ArmorStand getDisplayEntity() {
        return this.displayEntity.get();
    }

    public MinionData getTypeData() {
        return this.minionData;
    }

    public MinionData.MinionRank getRankData() {
        return this.minionData.getRank(this.rank);
    }

    public int getRank() {
        return this.rank;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public Location getCenterLocation() {
        return this.getLocation().add(0.5, 0.5, 0.5);
    }

    public World getWorld() {
        World world = this.location.getWorld();
        if (world == null)
            throw new IllegalStateException("Minion has no world!");
        return world;
    }

    public AnimationController getAnimationController() {
        return this.animationController;
    }

    public boolean isChunkLoaded() {
        return this.chunkLoaded;
    }

    public void setChunkLoaded(boolean chunkLoaded) {
        this.chunkLoaded = chunkLoaded;
    }

    public void openGui(Player player) {
        this.gui.openFor(player);
    }

    public void kickOutViewers() {
        this.gui.kickOutViewers();
    }

    private ArmorStand createDisplayEntity() {
        World world = this.location.getWorld();
        if (world == null)
            throw new IllegalStateException("Cannot create display entity for minion at " + this.location + " because the world is null");

        return world.spawn(this.getCenterLocation(), ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setSmall(true);
            entity.setCustomNameVisible(true);
            entity.setInvulnerable(true);
            entity.setCanPickupItems(false);
            entity.setPersistent(true);

            Arrays.stream(EquipmentSlot.values()).filter(x -> x != EquipmentSlot.HEAD).forEach(x -> {
                entity.addEquipmentLock(x, ArmorStand.LockType.ADDING_OR_CHANGING);
                entity.addEquipmentLock(x, ArmorStand.LockType.REMOVING_OR_CHANGING);
            });
        });
    }

    @Override
    public byte[] serialize() {
        return DataSerializable.write(outputStream -> {
            outputStream.writeUTF(this.minionData.getId());
            outputStream.writeInt(this.rank);
            outputStream.writeLong(this.owner.getMostSignificantBits());
            outputStream.writeLong(this.owner.getLeastSignificantBits());
            outputStream.writeUTF(this.getWorld().getName());
            outputStream.writeInt(this.location.getBlockX());
            outputStream.writeInt(this.location.getBlockY());
            outputStream.writeInt(this.location.getBlockZ());
            outputStream.writeBoolean(this.chunkLoaded);

            // TODO: Only write modified module settings
            outputStream.writeInt(this.modules.size());
            for (MinionModule module : this.modules.values()) {
                byte[] moduleData = module.serialize();
                outputStream.writeUTF(module.getName());
                outputStream.writeInt(moduleData.length);
                outputStream.write(moduleData);
            }

            // TODO: Only write modified animation settings
            byte[] animationData = this.animationController.serialize();
            outputStream.writeInt(animationData.length);
            outputStream.write(animationData);
        });
    }

    @Override
    public void deserialize(byte[] input) {
        DataSerializable.read(input, inputStream -> {
            String typeId = inputStream.readUTF();
            this.minionData = RoseMinions.getInstance().getManager(MinionTypeManager.class).getMinionData(typeId);
            if (this.minionData == null)
                throw new IllegalStateException("Minion type " + typeId + " no longer exists");

            this.rank = inputStream.readInt();
            this.loadRankData();

            this.owner = new UUID(inputStream.readLong(), inputStream.readLong());

            World world = Bukkit.getWorld(inputStream.readUTF());
            if (world == null)
                throw new IllegalStateException("Cannot create display entity for minion at " + this.location + " because the world is null");

            Location location = new Location(world, inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
            if (this.location == null)
                this.location = location;

            this.chunkLoaded = inputStream.readBoolean();

            int moduleCount = inputStream.readInt();
            for (int i = 0; i < moduleCount; i++) {
                String name = inputStream.readUTF();
                int moduleDataLength = inputStream.readInt();
                byte[] moduleData = new byte[moduleDataLength];
                inputStream.readFully(moduleData);

                // Find module with this name
                Optional<MinionModule> module = this.modules.values().stream()
                        .filter(x -> x.getName().equals(name))
                        .findFirst();

                if (module.isPresent()) {
                    module.get().deserialize(moduleData); // TODO: Make sure values are still within allowed range
                } else {
                    RoseMinions.getInstance().getLogger().warning("Skipped loading module " + name + " for minion at " + this.location + " because the module no longer exists");
                }
            }

            int animationDataLength = inputStream.readInt();
            byte[] animationData = new byte[animationDataLength];
            inputStream.readFully(animationData);
            this.animationController.deserialize(animationData);
            this.animationController.updateEntity();
        });
    }

}
