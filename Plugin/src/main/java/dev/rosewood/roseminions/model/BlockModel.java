package dev.rosewood.roseminions.model;

import org.bukkit.Location;

public interface BlockModel {

    void move(Location location);

    void remove();

    Location getLocation();

}
