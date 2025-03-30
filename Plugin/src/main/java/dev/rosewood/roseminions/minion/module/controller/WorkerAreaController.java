package dev.rosewood.roseminions.minion.module.controller;

import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.model.BlockPosition;
import dev.rosewood.roseminions.model.ChunkLocation;
import dev.rosewood.roseminions.model.WorkerAreaProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

public class WorkerAreaController<T> extends ModuleController {

    private WorkerAreaProperties properties;
    private final Consumer<Map<BlockPosition, T>> onUpdate;
    private final ScanBlockPredicate<T> predicate;
    private long nextUpdateTime;
    private Map<ChunkLocation, ChunkSnapshot> workerAreaChunks;
    private Map<BlockPosition, T> workerAreaData;
    private volatile boolean processing;

    public WorkerAreaController(MinionModule module, WorkerAreaProperties properties, Consumer<Map<BlockPosition, T>> onUpdate, ScanBlockPredicate<T> predicate) {
        super("worker_area", module);

        this.properties = properties;
        this.onUpdate = onUpdate;
        this.predicate = predicate;
        this.nextUpdateTime = System.currentTimeMillis() + 500;

        this.workerAreaChunks = Map.of();
        this.workerAreaData = Map.of();
    }

    public void setProperties(WorkerAreaProperties properties) {
        this.properties = properties;
    }

    @Override
    public void update() {
        if (!this.workerAreaData.isEmpty()) {
            this.onUpdate.accept(this.workerAreaData);
            this.workerAreaData = Map.of();
        }

        if (System.currentTimeMillis() >= this.nextUpdateTime && this.workerAreaChunks.isEmpty()) {
            this.nextUpdateTime = System.currentTimeMillis() + this.properties.updateFrequency();
            World world = this.module.getMinion().getWorld();
            this.workerAreaChunks = this.getChunkLocationsInRadius().stream()
                    .filter(chunk -> world.isChunkLoaded(chunk.x(), chunk.z()))
                    .map(chunk -> world.getChunkAt(chunk.x(), chunk.z()))
                    .collect(Collectors.toMap(chunk -> new ChunkLocation(world.getName(), chunk.getX(), chunk.getZ()), Chunk::getChunkSnapshot));
        }
    }

    @Override
    public void updateAsync() {
        if (this.workerAreaChunks.isEmpty() || this.processing)
            return;
        
        this.processing = true;
        int radius = this.properties.radius();
        int radiusSquared = radius * radius;
        boolean circle = this.properties.radiusType() == RadiusType.CIRCLE;

        Location centerLocation = this.module.getMinion().getLocation().add(this.properties.centerOffset());
        String world = centerLocation.getWorld().getName();
        int worldMin = centerLocation.getWorld().getMinHeight();

        int centerX = centerLocation.getBlockX();
        int centerY = centerLocation.getBlockY();
        int centerZ = centerLocation.getBlockZ();

        Map<BlockPosition, T> includedData = new LinkedHashMap<>();

        List<Point> positions = new ArrayList<>();
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                if (!circle || x * x + z * z <= radiusSquared)
                    positions.add(new Point(x, z));

        if (this.properties.shuffleScan())
            Collections.shuffle(positions);

        for (Point position : positions) {
            int targetX = centerX + position.x();
            int targetZ = centerZ + position.z();

            ChunkLocation chunkLocation = new ChunkLocation(world, targetX >> 4, targetZ >> 4);
            ChunkSnapshot chunkSnapshot = this.workerAreaChunks.get(chunkLocation);
            if (chunkSnapshot == null)
                continue; // Chunk not loaded

            int relativeX = targetX & 0xF;
            int relativeZ = targetZ & 0xF;

            int maxY = Math.min(centerY + radius, chunkSnapshot.getHighestBlockYAt(relativeX, relativeZ));
            int minY = Math.max(centerY - radius, worldMin);

            Function<Integer, Boolean> checkFunction = y -> {
                BlockScanObject<T> scanObject = this.predicate.test(relativeX, y, relativeZ, chunkSnapshot);
                return switch (scanObject.result()) {
                    case INCLUDE -> {
                        includedData.put(new BlockPosition(targetX, y, targetZ), scanObject.data());
                        yield false;
                    }
                    case INCLUDE_SKIP_COLUMN -> {
                        includedData.put(new BlockPosition(targetX, y, targetZ), scanObject.data());
                        yield true;
                    }
                    case SKIP_COLUMN -> true;
                    default -> false;
                };
            };

            switch (this.properties.scanDirection()) {
                case TOP_DOWN -> {
                    for (int y = maxY; y >= minY; y--)
                        if (checkFunction.apply(y))
                            break;
                }
                case BOTTOM_UP -> {
                    for (int y = minY; y <= maxY; y++)
                        if (checkFunction.apply(y))
                            break;
                }
            }
        }

        this.workerAreaChunks = Map.of();
        this.workerAreaData = includedData;
        this.processing = false;
    }

    private List<ChunkLocation> getChunkLocationsInRadius() {
        List<ChunkLocation> chunkLocations = new ArrayList<>();
        int radius = this.properties.radius();
        Location centerLocation = this.module.getMinion().getLocation().add(this.properties.centerOffset());
        String worldName = centerLocation.getWorld().getName();

        int minX = (centerLocation.getBlockX() - radius) >> 4;
        int minZ = (centerLocation.getBlockZ() - radius) >> 4;
        int maxX = (centerLocation.getBlockX() + radius) >> 4;
        int maxZ = (centerLocation.getBlockZ() + radius) >> 4;

        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++)
                chunkLocations.add(new ChunkLocation(worldName, x, z));

        return chunkLocations;
    }

    @Override
    public void unload() {

    }

    public enum RadiusType {
        CIRCLE,
        SQUARE
    }

    public enum ScanDirection {
        TOP_DOWN,
        BOTTOM_UP
    }

    public enum BlockScanResult {
        INCLUDE,
        INCLUDE_SKIP_COLUMN,
        EXCLUDE,
        SKIP_COLUMN
    }

    @FunctionalInterface
    public interface ScanBlockPredicate<T> {
        BlockScanObject<T> test(int x, int y, int z, ChunkSnapshot chunkSnapshot);
    }

    public record BlockScanObject<T>(BlockScanResult result, T data) {
        public BlockScanObject(BlockScanResult result) {
            this(result, null);
        }
    }

    private record Point(int x, int z) { }

}
