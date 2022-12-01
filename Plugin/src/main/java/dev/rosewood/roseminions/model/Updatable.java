package dev.rosewood.roseminions.model;

/**
 * Represents something that can be updated
 */
public interface Updatable {

    /**
     * Updates the object
     */
    void update();

    /**
     * Updates the object asynchronously
     */
    default void updateAsync() {
        // Does nothing by default
    }

}
