package dev.rosewood.roseminions.nms.v1_21_R5;

import dev.rosewood.roseminions.nms.NMSHandler;
import dev.rosewood.roseminions.nms.hologram.Hologram;
import dev.rosewood.roseminions.nms.util.DataSerializable;
import dev.rosewood.roseminions.nms.util.ReflectionUtils;
import dev.rosewood.roseminions.nms.v1_21_R5.entity.FakeFishingHook;
import dev.rosewood.roseminions.nms.v1_21_R5.hologram.HologramImpl;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;

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
        return DataSerializable.write(x -> {
            NbtIo.write((CompoundTag) net.minecraft.world.item.ItemStack.CODEC.encodeStart(
                    MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                    CraftItemStack.asNMSCopy(itemStack)
            ).getOrThrow(), x);
        });
    }

    @Override
    public ItemStack deserializeItemStack(byte[] bytes) {
        AtomicReference<ItemStack> itemStack = new AtomicReference<>();
        DataSerializable.read(bytes, x -> {
            itemStack.set(CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.CODEC.parse(
                    MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                    NbtIo.read(x)
            ).getOrThrow()));
        });
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

        return MinecraftServer.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING).getRandomItems(params)
                .stream()
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
        ((CraftEntity) entity).getHandle().moveOrInterpolateTo(new Vec3(location.getX(), location.getY(), location.getZ()), location.getYaw(), location.getPitch());
    }

    @Override
    public Hologram createHologram(Location location, List<String> text) {
        return new HologramImpl(text, location, entityCounter::incrementAndGet);
    }

    @Override
    public void setCustomNameUncapped(org.bukkit.entity.Entity entity, String customName) {
        ((CraftEntity) entity).getHandle().setCustomName(CraftChatMessage.fromStringOrNull(customName));
    }

    @Override
    public int getBlockExp(Block block, ItemStack tool) {
        ServerLevel nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
        BlockState state = nmsWorld.getBlockState(blockPos);
        return state.getBlock().getExpDrop(state, nmsWorld, blockPos, CraftItemStack.asNMSCopy(tool), true);
    }

}
