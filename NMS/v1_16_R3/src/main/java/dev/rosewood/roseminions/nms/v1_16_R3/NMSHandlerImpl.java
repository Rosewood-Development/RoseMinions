package dev.rosewood.roseminions.nms.v1_16_R3;

import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSHandler;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public byte[] serializeItemStacks(ItemStack[] itemStacks) {
        return DataSerializable.write(x -> {
            x.writeInt(itemStacks.length);
            for (ItemStack itemStack : itemStacks) {
                x.writeBoolean(itemStack != null);
                if (itemStack != null)
                    NBTCompressedStreamTools.a(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()), (DataOutput) x);
            }
        });
    }

    @Override
    public ItemStack[] deserializeItemStacks(byte[] bytes) {
        AtomicReference<ItemStack[]> itemStacks = new AtomicReference<>();
        DataSerializable.read(bytes, x -> {
            int length = x.readInt();
            ItemStack[] stacks = new ItemStack[length];
            for (int i = 0; i < length; i++)
                if (x.readBoolean())
                    stacks[i] = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R3.ItemStack.a(NBTCompressedStreamTools.a((DataInput) x)));
            itemStacks.set(stacks);
        });
        return itemStacks.get();
    }

}
