package dev.rosewood.roseminions.object;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public record BlockPosition(int x,
                            int y,
                            int z) {

    public Block toBlock(World world) {
        return world.getBlockAt(this.x, this.y, this.z);
    }

    public static BlockPosition from(Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
