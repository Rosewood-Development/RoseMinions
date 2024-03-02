package dev.rosewood.roseminions.nms.v1_17_R1;

import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSHandler;
import dev.rosewood.roseminions.nms.hologram.Hologram;
import dev.rosewood.roseminions.nms.util.ReflectionUtils;
import dev.rosewood.roseminions.nms.v1_17_R1.hologram.HologramImpl;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NMSHandlerImpl implements NMSHandler {

    private static AtomicInteger entityCounter; // Atomic integer to generate unique entity IDs, normally private

    static {
        try {
            entityCounter = (AtomicInteger) ReflectionUtils.getFieldByPositionAndType(net.minecraft.world.entity.Entity.class, 0, AtomicInteger.class).get(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] serializeItemStack(ItemStack itemStack) {
        return DataSerializable.write(x -> NbtIo.write(CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()), x));
    }

    @Override
    public ItemStack deserializeItemStack(byte[] bytes) {
        AtomicReference<ItemStack> itemStack = new AtomicReference<>();
        DataSerializable.read(bytes, x -> itemStack.set(CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.of(NbtIo.read(x)))));
        return itemStack.get();
    }

    @Override
    public void setPositionRotation(Entity entity, Location location) {
        ((CraftEntity) entity).getHandle().absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public Hologram createHologram(Location location, List<String> text) {
        return new HologramImpl(text, location, entityCounter::incrementAndGet);
    }

    @Override
    public void setCustomNameUncapped(org.bukkit.entity.Entity entity, String customName) {
        ((CraftEntity) entity).getHandle().setCustomName(CraftChatMessage.fromStringOrNull(customName));
    }

}
