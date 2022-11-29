package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.catching.CatchingBiConsumer;
import dev.rosewood.roseminions.util.catching.CatchingFunction;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SettingSerializer<T> {

    protected final Class<?> type;
    private final Function<T, String> toStringFunction;
    private final Function<String, T> fromStringFunction;

    public SettingSerializer(Class<?> type, Function<T, String> toStringFunction, Function<String, T> fromStringFunction) {
        this.type = type;
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

    public SettingSerializer(Class<?> type) {
        this(type, null, null);
    }

    public SettingSerializer() {
        this(null);
    }

    public abstract void write(CommentedConfigurationSection config, String key, T value, String... comments);

    public abstract byte[] write(T value);

    public abstract T read(ConfigurationSection config, String key);

    public abstract T read(byte[] input);

    public T readValue(byte[] input, CatchingFunction<ObjectInputStream, T> function) {
        AtomicReference<T> value = new AtomicReference<>();
        DataSerializable.read(input, x -> value.set(function.apply(x)));
        return value.get();
    }

    public byte[] writeValue(T value, CatchingBiConsumer<ObjectOutputStream, T> consumer) {
        return DataSerializable.write(x -> consumer.accept(x, value));
    }

    public final boolean isStringificationAllowed() {
        return this.toStringFunction != null && this.fromStringFunction != null;
    }

    public final String stringify(T value) {
        if (this.toStringFunction == null)
            throw new UnsupportedOperationException("stringify not implemented");
        return this.toStringFunction.apply(value);
    }

    public final T parseString(String value) {
        if (this.fromStringFunction == null)
            throw new UnsupportedOperationException("parseString not implemented");
        return this.fromStringFunction.apply(value);
    }

    public String getTypeName() {
        return this.type.getName();
    }

}
