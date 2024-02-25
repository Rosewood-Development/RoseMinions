package dev.rosewood.roseminions.nms.v1_16_R3.hologram;

import dev.rosewood.roseminions.nms.hologram.Hologram;
import dev.rosewood.roseminions.nms.hologram.HologramLine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.ParticleParamBlock;
import net.minecraft.server.v1_16_R3.Particles;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class HologramImpl extends Hologram {

    private static final List<DataWatcher.Item<?>> DATA_ITEMS = List.of(
            new DataWatcher.Item<>(DataWatcherRegistry.c.a(7), 0.5F),
            new DataWatcher.Item<>(DataWatcherRegistry.i.a(9), true),
            new DataWatcher.Item<>(DataWatcherRegistry.j.a(10), new ParticleParamBlock(Particles.BLOCK, Blocks.AIR.getBlockData()))
    );

    public HologramImpl(List<String> text, Location location, Supplier<Integer> entityIdSupplier) {
        super(text, location, entityIdSupplier);
    }

    @Override
    protected void create(Player player) {
        for (HologramLine line : this.hologramLines) {
            PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(
                    line.getEntityId(),
                    UUID.randomUUID(),
                    line.getLocation().getX(),
                    line.getLocation().getY(),
                    line.getLocation().getZ(),
                    90,
                    0,
                    EntityTypes.AREA_EFFECT_CLOUD,
                    1,
                    Vec3D.ORIGIN
            );

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    protected void update(Collection<Player> players, boolean force) {
        for (HologramLine line : this.hologramLines) {
            if (!force && !line.checkDirty())
                continue;

            List<DataWatcher.Item<?>> dataItems = new ArrayList<>(DATA_ITEMS);
            Optional<IChatBaseComponent> chatMessage = Optional.of(CraftChatMessage.fromStringOrNull(line.getText()));
            dataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.f.a(2), chatMessage));

            for (Player player : players) {
                Boolean visible = this.watchers.get(player);
                if (visible == null)
                    return;

                List<DataWatcher.Item<?>> allDataItems = new ArrayList<>(dataItems);
                allDataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.i.a(3), visible));

                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(line.getEntityId(), new DataWatcherWrapper(allDataItems), false));
            }
        }
    }

    @Override
    protected void delete(Player player) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.hologramLines.stream().mapToInt(HologramLine::getEntityId).toArray());

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private static class DataWatcherWrapper extends DataWatcher {

        private final List<DataWatcher.Item<?>> dataItems;

        public DataWatcherWrapper(List<DataWatcher.Item<?>> dataItems) {
            super(null);
            this.dataItems = dataItems;
        }

        @Override
        public List<DataWatcher.Item<?>> b() {
            return this.dataItems;
        }

    }

}
