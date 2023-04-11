package dev.rosewood.roseminions.nms.v1_19_R2;

import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.nms.NMSHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;

public class NMSHandlerImpl implements NMSHandler {

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
    public List<ItemStack> getFishingLoot(Entity looter, Location location, ItemStack fishingRod) {
        ServerLevel level = ((CraftWorld) looter.getWorld()).getHandle();
        ResourceLocation resourceLocation = CraftNamespacedKey.toMinecraft(LootTables.FISHING.getKey());
        LootContext context = new LootContext.Builder(level)
                .withParameter(LootContextParams.ORIGIN, new Vec3(location.getX(), location.getY(), location.getZ()))
                .withParameter(LootContextParams.TOOL, CraftItemStack.asNMSCopy(fishingRod))
                .withParameter(LootContextParams.THIS_ENTITY, ((CraftEntity) looter).getHandle()) // TODO: This is not the correct entity and needs to be a FishingHook, we might be able to just mock it though
                .create(LootContextParamSets.FISHING);

        return level.getServer().getLootTables().get(resourceLocation).getRandomItems(context).stream()
                .filter(x -> !x.isEmpty())
                .map(CraftItemStack::asBukkitCopy)
                .toList();
    }

    @Override
    public void setPositionRotation(Entity entity, Location location) {
        ((CraftEntity) entity).getHandle().absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

}
