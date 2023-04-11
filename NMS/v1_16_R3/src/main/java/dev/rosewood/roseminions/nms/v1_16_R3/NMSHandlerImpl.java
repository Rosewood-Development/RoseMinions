package dev.rosewood.roseminions.nms.v1_16_R3;

import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSHandler;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public byte[] serializeItemStack(ItemStack itemStack) {
        return DataSerializable.write(x -> NBTCompressedStreamTools.a(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()), (DataOutput) x));
    }

    @Override
    public ItemStack deserializeItemStack(byte[] bytes) {
        AtomicReference<ItemStack> itemStack = new AtomicReference<>();
        DataSerializable.read(bytes, x -> itemStack.set(CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R3.ItemStack.a(NBTCompressedStreamTools.a((DataInput) x)))));
        return itemStack.get();
    }

    @Override
    public void setPositionRotation(Entity entity, Location location) {
        ((CraftEntity) entity).getHandle().setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

}
