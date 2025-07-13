package dev.rosewood.roseminions.nms.v1_20_R4;

import dev.rosewood.roseminions.nms.NMSHandler;
import dev.rosewood.roseminions.nms.hologram.Hologram;
import dev.rosewood.roseminions.nms.util.DataSerializable;
import dev.rosewood.roseminions.nms.util.ReflectionUtils;
import dev.rosewood.roseminions.nms.v1_20_R4.entity.FakeFishingHook;
import dev.rosewood.roseminions.nms.v1_20_R4.hologram.HologramImpl;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;

public class NMSHandlerImpl implements NMSHandler {

    private static AtomicInteger entityCounter; // Atomic integer to generate unique entity IDs, normally private
    private static FakeFishingHook fishingHook;

    static {
        try {
            entityCounter = (AtomicInteger) ReflectionUtils.getFieldByPositionAndType(net.minecraft.world.entity.Entity.class, 0, AtomicInteger.class).get(null);
            fishingHook = new FakeFishingHook(((CraftWorld) Bukkit.getWorlds().get(0)).getHandle());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] serializeItemStack(ItemStack itemStack) {
        return DataSerializable.write(x -> NbtIo.write((CompoundTag) CraftItemStack.asNMSCopy(itemStack).save(MinecraftServer.getServer().registryAccess()), x));
    }

    @Override
    public ItemStack deserializeItemStack(byte[] bytes) {
        AtomicReference<ItemStack> itemStack = new AtomicReference<>();
        DataSerializable.read(bytes, x -> itemStack.set(CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.parse(MinecraftServer.getServer().registryAccess(), NbtIo.read(x)).orElseThrow())));
        return itemStack.get();
    }

    @Override
    public List<ItemStack> getFishingLoot(Entity looter, Location location, ItemStack fishingRod) {
        fishingHook.setOpenWater(location);

        ServerLevel level = ((CraftWorld) looter.getWorld()).getHandle();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, new Vec3(location.getX(), location.getY(), location.getZ()))
                .withOptionalParameter(LootContextParams.TOOL, CraftItemStack.asNMSCopy(fishingRod))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, fishingHook)
                .create(LootContextParamSets.FISHING);

        ResourceLocation resourceLocation = CraftNamespacedKey.toMinecraft(LootTables.FISHING.getKey());
        Registry<LootTable> registry = MinecraftServer.getServer().registryAccess().registry(Registries.LOOT_TABLE).orElseThrow();
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, resourceLocation);
        return registry.getOrThrow(lootTableKey).getRandomItems(params).stream()
                .filter(x -> !x.isEmpty())
                .map(CraftItemStack::asBukkitCopy)
                .toList();
    }

    @Override
    public FishHook getLastFishHook() {
        return (FishHook) fishingHook.getBukkitEntity();
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
