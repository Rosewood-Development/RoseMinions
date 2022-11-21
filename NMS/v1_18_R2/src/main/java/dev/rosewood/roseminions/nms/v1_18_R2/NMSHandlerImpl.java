package dev.rosewood.roseminions.nms.v1_18_R2;

import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSHandler;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public byte[] serializeItemStacks(ItemStack[] itemStacks) {
        return DataSerializable.write(x -> {
            x.writeInt(itemStacks.length);
            for (ItemStack itemStack : itemStacks) {
                x.writeBoolean(itemStack != null);
                if (itemStack != null)
                    NbtIo.write(CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()), x);
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
                    stacks[i] = CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.of(NbtIo.read(x)));
            itemStacks.set(stacks);
        });
        return itemStacks.get();
    }

}
