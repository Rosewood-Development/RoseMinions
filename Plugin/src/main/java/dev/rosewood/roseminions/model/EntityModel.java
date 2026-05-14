package dev.rosewood.roseminions.model;

import org.bukkit.Location;

public interface EntityModel {

    void move(Location location);

    void remove();

    Location getLocation();

}
