package dev.rosewood.roseminions.minion;

import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.controller.AnimationController;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.model.DataSerializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;

public class Minion implements DataSerializable {

    private Reference<ArmorStand> displayEntity;
    private String typeId;
    private int rank;
    private UUID owner;
    private Location location;
    private boolean chunkLoaded;
    private final Map<Class<? extends MinionModule>, MinionModule> modules;

    private final AnimationController animationController;

    private Minion(ArmorStand displayEntity) {
        this.displayEntity = new WeakReference<>(displayEntity);
        this.modules = new HashMap<>();
        this.animationController = new AnimationController(this);
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
    public Minion(String typeId, int rank, UUID owner, Location location, boolean chunkLoaded) {
        this.displayEntity = new WeakReference<>(null);
        this.typeId = typeId;
        this.rank = rank;
        this.owner = owner;

        this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.chunkLoaded = chunkLoaded;
        this.modules = new HashMap<>();
        this.animationController = new AnimationController(this);
    }

    public void setModules(Collection<MinionModule> modules) {
        this.modules.clear();
        this.modules.putAll(modules.stream().collect(HashMap::new, (map, module) -> map.put(module.getClass(), module), HashMap::putAll));
    }

    @SuppressWarnings("unchecked")
    public <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass) {
        return Optional.ofNullable((T) this.modules.get(moduleClass));
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

    public ArmorStand getDisplayEntity() {
        return this.displayEntity.get();
    }

    public String getTypeId() {
        return this.typeId;
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
            outputStream.writeUTF(this.typeId);
            outputStream.writeLong(this.owner.getMostSignificantBits());
            outputStream.writeLong(this.owner.getLeastSignificantBits());
            outputStream.writeUTF(this.getWorld().getName());
            outputStream.writeInt(this.location.getBlockX());
            outputStream.writeInt(this.location.getBlockY());
            outputStream.writeInt(this.location.getBlockZ());
            outputStream.writeBoolean(this.chunkLoaded);

            outputStream.writeInt(this.modules.size());
            for (MinionModule module : this.modules.values()) {
                byte[] moduleData = module.serialize();
                outputStream.writeUTF(module.getName());
                outputStream.writeInt(moduleData.length);
                outputStream.write(moduleData);
            }

            byte[] animationData = this.animationController.serialize();
            outputStream.writeInt(animationData.length);
            outputStream.write(animationData);
        });
    }

    @Override
    public void deserialize(byte[] input) {
        DataSerializable.read(input, inputStream -> {
            this.typeId = inputStream.readUTF();
            this.owner = new UUID(inputStream.readLong(), inputStream.readLong());

            World world = Bukkit.getWorld(inputStream.readUTF());
            if (world == null)
                throw new IllegalStateException("Cannot create display entity for minion at " + this.location + " because the world is null");

            Location location = new Location(world, inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
            if (this.location == null)
                this.location = location;

            this.chunkLoaded = inputStream.readBoolean();

            MinionModuleManager moduleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);

            int moduleCount = inputStream.readInt();
            for (int i = 0; i < moduleCount; i++) {
                String name = inputStream.readUTF();
                int moduleDataLength = inputStream.readInt();
                byte[] moduleData = new byte[moduleDataLength];
                inputStream.read(moduleData);

                MinionModule module = moduleManager.createModule(name, this);
                if (module != null) {
                    module.deserialize(moduleData);
                    this.modules.put(module.getClass(), module);
                } else {
                    RoseMinions.getInstance().getLogger().warning("Skipped loading module " + name + " for minion at " + this.location + " because it does not exist");
                }
            }

            int animationDataLength = inputStream.readInt();
            byte[] animationData = new byte[animationDataLength];
            inputStream.read(animationData);
            this.animationController.deserialize(animationData);
            this.animationController.updateEntity();
        });
    }

}
