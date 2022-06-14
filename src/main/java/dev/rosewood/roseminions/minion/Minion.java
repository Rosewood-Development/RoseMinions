package dev.rosewood.roseminions.minion;

import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.animation.MinionAnimation;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.model.ObjectSerializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;

public class Minion implements ObjectSerializable {

    private Reference<ArmorStand> displayEntity;
    private UUID owner;
    private Location location;
    private boolean chunkLoaded;
    private final Map<String, MinionModule> modules;

    private MinionAnimation animation;

    public Minion(ArmorStand displayEntity) {
        this.displayEntity = new WeakReference<>(displayEntity);
        this.modules = new HashMap<>();
    }

    public Minion(UUID owner, Location location, boolean chunkLoaded) {
        this.displayEntity = new WeakReference<>(null);
        this.owner = owner;
        this.location = location;
        this.chunkLoaded = chunkLoaded;
        this.modules = new HashMap<>();
    }

    public void setModules(Map<String, MinionModule> modules) {
        this.modules.clear();
        this.modules.putAll(modules);
    }

    public void setAnimation(MinionAnimation animation) {
        this.animation = animation;
    }

    public void update() {
        ArmorStand displayEntity = this.displayEntity.get();
        if (displayEntity == null || !displayEntity.isValid()) {
            this.displayEntity = new WeakReference<>(this.createDisplayEntity());
            this.animation.updateEntity();
        }

        if (this.animation != null)
            this.animation.update();

        this.modules.values().forEach(MinionModule::update);
    }

    public ArmorStand getDisplayEntity() {
        return this.displayEntity.get();
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Location getLocation() {
        return this.location;
    }

    public Location getDisplayLocation() {
        return this.location.clone().add(0.5, 0.75, 0.5);
    }

    public World getWorld() {
        World world = this.location.getWorld();
        if (world == null)
            throw new IllegalStateException("Minion has no world!");
        return world;
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

        return world.spawn(this.getDisplayLocation(), ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setSmall(true);
            entity.setCustomNameVisible(true);
            entity.setInvulnerable(true);
            entity.setCanPickupItems(false);
            entity.setPersistent(true);

            Arrays.stream(EquipmentSlot.values()).forEach(x -> {
                entity.addEquipmentLock(x, ArmorStand.LockType.ADDING_OR_CHANGING);
                entity.addEquipmentLock(x, ArmorStand.LockType.REMOVING_OR_CHANGING);
            });
        });
    }

    @Override
    public void serialize(ObjectOutputStream outputStream) throws IOException {
        World world = this.location.getWorld();
        if (world == null)
            throw new IllegalStateException("Cannot create display entity for minion at " + this.location + " because the world is null");

        outputStream.writeLong(this.owner.getMostSignificantBits());
        outputStream.writeLong(this.owner.getLeastSignificantBits());
        outputStream.writeUTF(world.getName());
        outputStream.writeInt(this.location.getBlockX());
        outputStream.writeInt(this.location.getBlockY());
        outputStream.writeInt(this.location.getBlockZ());
        outputStream.writeBoolean(this.chunkLoaded);

        outputStream.writeInt(this.modules.size());
        for (Map.Entry<String, MinionModule> entry : this.modules.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            entry.getValue().serialize(outputStream);
        }
    }

    @Override
    public void deserialize(ObjectInputStream inputStream) throws IOException {
        this.owner = new UUID(inputStream.readLong(), inputStream.readLong());
        this.location = new Location(Bukkit.getWorld(inputStream.readUTF()), inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
        this.chunkLoaded = inputStream.readBoolean();

        MinionModuleManager moduleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);

        int moduleCount = inputStream.readInt();
        for (int i = 0; i < moduleCount; i++) {
            String name = inputStream.readUTF();
            MinionModule module = this.modules.get(name);
            if (module != null) {
                module.deserialize(inputStream);
            } else {
                RoseMinions.getInstance().getLogger().warning("Skipped loading module " + name + " for minion at " + this.location + " because it does not exist");
            }
        }
    }

}
