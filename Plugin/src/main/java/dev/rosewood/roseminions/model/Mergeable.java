package dev.rosewood.roseminions.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Mergeable<T> {

    /**
     * Combines this object with another and returns a new object, preferring values in this object
     *
     * @param other the other object
     * @return a new object with a combination of both objects' values, this object being preferred
     */
    T merge(T other);

    static <T> T merge(T base, T other) {
        return other != null ? other : base;
    }

    static <T> List<T> mergeList(List<T> base, List<T> other) {
        List<T> combined = new ArrayList<>(base.size() + other.size());
        combined.addAll(base);
        combined.addAll(other);
        return combined;
    }

    static <K, V> Map<K, V> mergeMap(Map<K, V> base, Map<K, V> other) {
        Map<K, V> combined = new HashMap<>((int) Math.ceil((base.size() + other.size()) / 0.75));
        combined.putAll(other);
        combined.putAll(base);
        return combined;
    }

}
