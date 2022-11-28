package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.catching.CatchingBiConsumer;
import dev.rosewood.roseminions.util.catching.CatchingFunction;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.configuration.ConfigurationSection;

public abstract class SettingSerializer<T> {

    protected final Class<?> type;
    private final boolean supportsStringification;

    public SettingSerializer(Class<?> type) {
        this.type = type;

        // If an UnsupportedOperationException is thrown, stringification is not supported
        // If anything else happens, it is
        boolean stringificationAllowed;
        try {
            this.stringify(null);
            this.parseString(null);
            stringificationAllowed = true;
        } catch (UnsupportedOperationException e) {
            stringificationAllowed = false;
        } catch (Exception e) {
            stringificationAllowed = true;
        }
        this.supportsStringification = stringificationAllowed;
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
        return this.supportsStringification;
    }

    public String stringify(T value) {
        throw new UnsupportedOperationException("stringify not implemented");
    }

    public T parseString(String value) {
        throw new UnsupportedOperationException("parseString not properly implemented");
    }

    public String getTypeName() {
        return this.type.getName();
    }

}
