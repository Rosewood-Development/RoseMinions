package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class SettingSerializer<T> {

    protected final Class<T> type;
    protected final PersistentDataType<?, T> persistentDataType;
    private final Function<T, String> toStringFunction;
    private final Function<String, T> fromStringFunction;

    public SettingSerializer(Class<T> type, PersistentDataType<?, T> persistentDataType, Function<T, String> toStringFunction, Function<String, T> fromStringFunction) {
        this.type = type;
        this.persistentDataType = persistentDataType;
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

    public SettingSerializer(Class<T> type, PersistentDataType<?, T> persistentDataType) {
        this(type, persistentDataType, null, null);
    }

    public SettingSerializer(PersistentDataType<?, T> persistentDataType) {
        this(null, persistentDataType);
    }

    public abstract void write(CommentedConfigurationSection config, String key, T value, String... comments);

    public void write(PersistentDataContainer container, PersistentDataAdapterContext context, String key, T value) {
        container.set(CustomPersistentDataType.KeyHelper.get(key), this.persistentDataType, value);
    }

    public abstract T read(ConfigurationSection config, String key);

    public T read(PersistentDataContainer container, String key) {
        return container.get(CustomPersistentDataType.KeyHelper.get(key), this.persistentDataType);
    }

    public final boolean isStringificationAllowed() {
        return this.toStringFunction != null && this.fromStringFunction != null;
    }

    public final String stringify(T value) {
        if (this.toStringFunction == null)
            throw new UnsupportedOperationException("stringify not implemented, check isStringificationAllowed() first");
        return this.toStringFunction.apply(value);
    }

    public final T parseString(String value) {
        if (this.fromStringFunction == null)
            throw new UnsupportedOperationException("parseString not implemented, check isStringificationAllowed() first");
        return this.fromStringFunction.apply(value);
    }

    public PersistentDataType<?, T> getPersistentDataType() {
        return this.persistentDataType;
    }

    public Class<T> getType() {
        return this.type;
    }

    public String getTypeName() {
        return this.type.getName();
    }

}
