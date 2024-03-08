package dev.rosewood.roseminions.model;

import org.bukkit.World;
import org.bukkit.block.Block;

public record BlockPosition(int x,
                            int y,
                            int z) {

    public Block toBlock(World world) {
        return world.getBlockAt(this.x, this.y, this.z);
    }

}
