package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Allows reading and writing an object type to/from both YAML and PDC.
 * Supports comments for writing to YAML.
 * @param <T> The serialized type
 */
public abstract class SettingSerializer<T> {

    protected final Class<T> type;
    protected final PersistentDataType<?, T> persistentDataType;
    private final Function<T, String> asStringFunction;
    private final Function<String, T> fromStringFunction;

    public SettingSerializer(Class<T> type, PersistentDataType<?, T> persistentDataType, Function<T, String> asStringFunction, Function<String, T> fromStringFunction) {
        this.type = type;
        this.persistentDataType = persistentDataType;
        this.asStringFunction = asStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

    public SettingSerializer(Class<T> type, PersistentDataType<?, T> persistentDataType) {
        this(type, persistentDataType, null, null);
    }

    public SettingSerializer(PersistentDataType<?, T> persistentDataType) {
        this(null, persistentDataType);
    }

    /**
     * Writes an object to a ConfigurationSection, including comments if it is a CommentedConfigurationSection
     *
     * @param config The ConfigurationSection to write to
     * @param key The key path to save in the config
     * @param value The value to save
     * @param comments Comments to write above the setting in the config
     */
    public abstract void write(ConfigurationSection config, String key, T value, String... comments);

    /**
     * Writes an object to a PersistentDataContainer
     *
     * @param container The PersistentDataContainer to write to
     * @param key The key path to save in the config, if not namespaced, will be prefixed with the plugin path
     * @param value The value to save
     */
    public void write(PersistentDataContainer container, String key, T value) {
        container.set(CustomPersistentDataType.KeyHelper.get(key), this.persistentDataType, value);
    }

    /**
     * Reads an object from a ConfigurationSection
     *
     * @param config The ConfigurationSection to read from
     * @param key The path to the value in the config
     * @return the value read
     */
    public abstract T read(ConfigurationSection config, String key);

    /**
     * Reads an object from a PersistentDataContainer
     *
     * @param container The PersistentDataContainer to read from
     * @param key The key of the value in the container
     * @return the value read
     */
    public T read(PersistentDataContainer container, String key) {
        return container.get(CustomPersistentDataType.KeyHelper.get(key), this.persistentDataType);
    }

    public final boolean isStringKey() {
        return this.asStringFunction != null && this.fromStringFunction != null;
    }

    public final String asStringKey(T key) {
        if (this.asStringFunction == null)
            throw new UnsupportedOperationException("asStringKey not available, check isStringKey() first");
        return this.asStringFunction.apply(key);
    }

    public final T fromStringKey(String key) {
        if (this.fromStringFunction == null)
            throw new UnsupportedOperationException("fromStringKey not available, check isStringKey() first");
        return this.fromStringFunction.apply(key);
    }

    public Class<T> getType() {
        return this.type;
    }

}
