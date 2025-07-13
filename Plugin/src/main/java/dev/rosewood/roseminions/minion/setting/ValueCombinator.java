package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.object.Mergeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ValueCombinator<T> {

    private final Supplier<T> baseValueSupplier;
    private final List<ValueCombinator<T>> others;
    private final boolean mergeable;

    public ValueCombinator(Supplier<T> baseValueSupplier) {
        this.baseValueSupplier = baseValueSupplier;
        this.others = new ArrayList<>();
        this.mergeable = baseValueSupplier.get() instanceof Mergeable;
    }

    public ValueCombinator(ValueCombinator<T> other) {
        this.baseValueSupplier = other.baseValueSupplier;
        this.others = other.others.stream().map(ValueCombinator::new).collect(Collectors.toCollection(ArrayList::new));
        this.mergeable = other.mergeable;
    }

    public void combine(ValueCombinator<T> value) {
        this.others.add(value);
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (this.others.isEmpty())
            return this.baseValueSupplier.get();

        if (!this.mergeable) {
            // Not mergeable, return the first non-null value in reverse order
            for (int i = this.others.size() - 1; i >= 0; i--) {
                T value = this.others.get(i).get();
                if (value != null)
                    return value;
            }
            return this.baseValueSupplier.get();
        } else {
            // Overlay values on top of the base value
            Mergeable<T> mergeable = (Mergeable<T>) this.baseValueSupplier.get();
            for (ValueCombinator<T> other : this.others) {
                if (mergeable == null) {
                    mergeable = (Mergeable<T>) other.get();
                    continue;
                }

                T otherValue = other.get();
                if (otherValue != null)
                    mergeable = (Mergeable<T>) mergeable.merge(otherValue);
            }
            return (T) mergeable;
        }
    }

}
