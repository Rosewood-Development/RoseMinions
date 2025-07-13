package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.nms.NMSAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class FishingBobberModel implements BlockModel {

    private static final BlockData WHITE_CONCRETE = Material.WHITE_CONCRETE.createBlockData();
    private static final BlockData RED_CONCRETE = Material.RED_CONCRETE.createBlockData();
    private static final BlockData BLACK_CONCRETE = Material.BLACK_CONCRETE.createBlockData();

    private final BlockDisplay blockDisplay;

    public FishingBobberModel(Location location) {
        this.blockDisplay = this.summonBlockDisplays(location);
    }

    public void move(Location location) {
        NMSAdapter.getHandler().setPositionRotation(this.blockDisplay, location);
    }

    public void remove() {
        this.blockDisplay.remove();
    }

    public Location getLocation() {
        return this.blockDisplay.getLocation();
    }

    private BlockDisplay summonBlockDisplays(Location location) {
        // Summon the main block display
        BlockDisplay mainDisplay = (BlockDisplay) location.getWorld().spawnEntity(
                location.clone().add(-0.5, -0.5, -0.5),
                EntityType.BLOCK_DISPLAY
        );

        // Create passengers
        BlockDisplay[] passengers = new BlockDisplay[6];

        // Passenger 1 - red concrete
        passengers[0] = this.createPassenger(location, RED_CONCRETE, new Transformation(
                new Vector3f(-0.0625f, 0.0625f, -0.1250f),
                new AxisAngle4f(0f, 0f, 0f, 1f),
                new Vector3f(0.1875f, 0.0625f, 0.1875f),
                new AxisAngle4f(0f, 0f, 0f, 1f)
        ));

        // Passenger 2 - white concrete (first one)
        passengers[1] = this.createPassenger(location, WHITE_CONCRETE, new Transformation(
                new Vector3f(-0.0625f, 0.0000f, -0.1250f),
                new AxisAngle4f(0f, 0f, 0f, 1f),
                new Vector3f(0.1875f, 0.0625f, 0.1875f),
                new AxisAngle4f(0f, 0f, 0f, 1f)
        ));

        // Passenger 3 - white concrete (second one)
        passengers[2] = this.createPassenger(location, WHITE_CONCRETE, new Transformation(
                new Vector3f(-0.0625f, 0.1250f, -0.1250f),
                new AxisAngle4f(0f, 0f, 0f, 1f),
                new Vector3f(0.1875f, 0.0625f, 0.1875f),
                new AxisAngle4f(0f, 0f, 0f, 1f)
        ));

        // Passenger 4 - black concrete (first one)
        passengers[3] = this.createPassenger(location, BLACK_CONCRETE, new Transformation(
                new Vector3f(0.0000f, -0.1875f, -0.0625f),
                new AxisAngle4f(0f, 0f, 0f, 1f),
                new Vector3f(0.0625f, 0.1875f, 0.0625f),
                new AxisAngle4f(0f, 0f, 0f, 1f)
        ));

        // Passenger 5 - black concrete (second one)
        passengers[4] = this.createPassenger(location, BLACK_CONCRETE, new Transformation(
                new Vector3f(0.0000f, -0.1875f, -0.1250f),
                new AxisAngle4f(0f, 0f, 0f, 1f),
                new Vector3f(0.0625f, 0.0625f, 0.0625f),
                new AxisAngle4f(0f, 0f, 0f, 1f)
        ));

        // Passenger 6 - black concrete (third one)
        passengers[5] = this.createPassenger(location, BLACK_CONCRETE, new Transformation(
                new Vector3f(0.0000f, -0.1250f, -0.1875f),
                new AxisAngle4f(0f, 0f, 0f, 1f),
                new Vector3f(0.0625f, 0.0625f, 0.0625f),
                new AxisAngle4f(0f, 0f, 0f, 1f)
        ));

        // Add all passengers to the main display
        for (BlockDisplay passenger : passengers)
            mainDisplay.addPassenger(passenger);

        return mainDisplay;
    }

    private BlockDisplay createPassenger(Location location, BlockData blockData, Transformation transformation) {
        return location.getWorld().spawn(location, BlockDisplay.class, x -> {
            x.setBlock(blockData);
            x.setTransformation(transformation);
            x.setPersistent(false);
        });
    }

}
