package dev.rosewood.roseminions.minion.module.controller;

import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.model.BlockPosition;
import dev.rosewood.roseminions.model.ChunkLocation;
import dev.rosewood.roseminions.model.WorkerAreaProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private final long shuffleSeed = MinionUtils.RANDOM.nextLong();
    private final boolean worldHeightOptimization;
    private long nextUpdateTime;
    private Map<ChunkLocation, ChunkSnapshot> workerAreaChunks;
    private Map<BlockPosition, T> workerAreaData;
    private volatile boolean processing;

    public WorkerAreaController(MinionModule module, WorkerAreaProperties properties, Consumer<Map<BlockPosition, T>> onUpdate, ScanBlockPredicate<T> predicate, boolean worldHeightOptimization) {
        super("worker_area", module);

        this.properties = properties;
        this.onUpdate = onUpdate;
        this.predicate = predicate;
        this.worldHeightOptimization = worldHeightOptimization;
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
        boolean circle = this.properties.scanShape() == ScanShape.CYLINDER || this.properties.scanShape() == ScanShape.SPHERE;
        boolean roundHeight = this.properties.scanShape() == ScanShape.SPHERE;

        Location minionLocation = this.module.getMinion().getLocation();
        Location centerLocation = minionLocation.clone().add(this.properties.centerOffset());
        World world = centerLocation.getWorld();
        String worldName = world.getName();
        int worldMax = centerLocation.getWorld().getMaxHeight();
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
            Collections.shuffle(positions, new Random(this.shuffleSeed));

        for (Point position : positions) {
            int targetX = centerX + position.x();
            int targetZ = centerZ + position.z();

            int relativeX = targetX & 0xF;
            int relativeZ = targetZ & 0xF;

            ChunkLocation chunkLocation = new ChunkLocation(worldName, targetX >> 4, targetZ >> 4);
            ChunkSnapshot chunkSnapshot = this.workerAreaChunks.get(chunkLocation);
            if (chunkSnapshot == null)
                continue; // Chunk not loaded

            int maxY = Math.min(centerY + radius, this.worldHeightOptimization ? worldMax : chunkSnapshot.getHighestBlockYAt(relativeX, relativeZ));
            int minY = Math.max(centerY - radius, worldMin);

            Function<Integer, Boolean> checkFunction = y -> {
                if (roundHeight) {
                    Location location = new Location(world, targetX, y, targetZ);
                    if (location.distanceSquared(centerLocation) > radiusSquared)
                        return false;
                }

                BlockScanResult<T> scanObject = this.predicate.test(relativeX, y, relativeZ, chunkSnapshot);
                return switch (scanObject.type()) {
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

        // Don't let the minion directly manage the block it's in or the one above it
        BlockPosition selfPosition = new BlockPosition(minionLocation.getBlockX(), minionLocation.getBlockY(), minionLocation.getBlockZ());
        BlockPosition abovePosition = new BlockPosition(minionLocation.getBlockX(), minionLocation.getBlockY() + 1, minionLocation.getBlockZ());;
        includedData.remove(selfPosition);
        includedData.remove(abovePosition);

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

    public enum ScanShape {
        CYLINDER,
        SPHERE,
        CUBE
    }

    public enum ScanDirection {
        TOP_DOWN,
        BOTTOM_UP
    }

    public enum BlockScanResultType {
        INCLUDE,
        INCLUDE_SKIP_COLUMN,
        EXCLUDE,
        SKIP_COLUMN
    }

    @FunctionalInterface
    public interface ScanBlockPredicate<T> {
        BlockScanResult<T> test(int x, int y, int z, ChunkSnapshot chunkSnapshot);
    }

    public static class BlockScanResult<T> {
        private static final BlockScanResult<?> EXCLUDE_INSTANCE = new BlockScanResult<>(BlockScanResultType.EXCLUDE, null);
        private static final BlockScanResult<?> SKIP_COLUMN_INSTANCE = new BlockScanResult<>(BlockScanResultType.SKIP_COLUMN, null);

        private final BlockScanResultType type;
        private final T data;

        private BlockScanResult(BlockScanResultType type, T data) {
            this.type = type;
            this.data = data;
        }

        public BlockScanResultType type() {
            return this.type;
        }

        public T data() {
            return this.data;
        }

        public static <T> BlockScanResult<T> include(T data) {
            return new BlockScanResult<>(BlockScanResultType.INCLUDE, data);
        }

        public static <T> BlockScanResult<T> includeSkipColumn(T data) {
            return new BlockScanResult<>(BlockScanResultType.INCLUDE_SKIP_COLUMN, data);
        }

        @SuppressWarnings("unchecked")
        public static <T> BlockScanResult<T> exclude() {
            return (BlockScanResult<T>) EXCLUDE_INSTANCE;
        }

        @SuppressWarnings("unchecked")
        public static <T> BlockScanResult<T> skipColumn() {
            return (BlockScanResult<T>) SKIP_COLUMN_INSTANCE;
        }
    }

    private record Point(int x, int z) { }

}
