package dev.rosewood.roseminions.model;

public interface Mergeable<T> {

    /**
     * Combines this object with another and returns a new object, preferring values in the other object if available
     *
     * @param other the other object
     * @return a new object with a combination of both objects' values, non-null values in the other object being preferred
     */
    T merge(T other);

    static <T> T merge(T base, T other) {
        return other != null ? other : base;
    }

}
