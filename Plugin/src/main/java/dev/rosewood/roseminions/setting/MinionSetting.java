package dev.rosewood.roseminions.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;

public interface MinionSetting<T> {

    /**
     * Writes the setting to the given config.
     *
     * @param config the config to write to
     */
    void write(ConfigurationSection config);

    /**
     * Writes the setting and its default value to the given config.
     *
     * @param config the config to write to
     */
    void writeWithDefault(ConfigurationSection config);

    /**
     * Reads the setting from the given config and returns it.
     *
     * @param config the config to read from
     * @return the setting value
     */
    T read(ConfigurationSection config);

    /**
     * Reads the setting from the given config and sets it as the default value supplier.
     *
     * @param config the config to read from
     */
    void readDefault(ConfigurationSection config);

    /**
     * Checks if the setting exists in the given config and has all properties written to
     *
     * @param config the config to read from
     * @return true if the setting exists in the given config
     */
    boolean readIsValid(ConfigurationSection config);

    /**
     * @return the serializer for this setting
     */
    DataSerializer<T> getSerializer();

    /**
     * @return the key name of this setting
     */
    String getKey();

    /**
     * @return a new instance of the default value
     */
    T getDefaultValue();

    /**
     * @return the comments detailing this setting
     */
    String[] getComments();

    /**
     * @return the value of this setting from the given RoseConfig
     */
    default T get(MinionConfig config) {
        return config.get(this);
    }

    /**
     * @return true if this setting should not be written as YAML, it will still be saved as PDC
     */
    boolean isHidden();

    /**
     * Creates a new RoseSetting as a copy of this RoseSetting but with a different default value and optionally comments.
     * This should only be used for primitives or immutable types. Instances will be shared.
     * Comments can be removed by setting them to null, empty comments will cause them to be copied from the original.
     *
     * @param defaultValue the new default value
     * @return a copy of this RoseSetting with a different default value
     */
    MinionSetting<T> copy(T defaultValue, String... comments);

    /**
     * Creates a new RoseSetting as a copy of this RoseSetting but with a different default value.
     * This should be used for mutable types where the underlying value may be changed during the lifetime of
     * this setting.
     * Comments can be removed by setting them to null, empty comments will cause them to be copied from the original.
     *
     * @param defaultValueSupplier the new default value supplier
     * @return a copy of this RoseSetting with a different default value
     */
    MinionSetting<T> copy(Supplier<T> defaultValueSupplier, String... comments);

    static MinionSetting<Boolean> ofBoolean(String name, boolean defaultValue, String... comments) {
        return of(name, DataSerializers.BOOLEAN, () -> defaultValue, comments);
    }

    static MinionSetting<Integer> ofInteger(String name, int defaultValue, String... comments) {
        return of(name, DataSerializers.INTEGER, () -> defaultValue, comments);
    }

    static MinionSetting<Long> ofLong(String name, long defaultValue, String... comments) {
        return of(name, DataSerializers.LONG, () -> defaultValue, comments);
    }

    static MinionSetting<Short> ofShort(String name, short defaultValue, String... comments) {
        return of(name, DataSerializers.SHORT, () -> defaultValue, comments);
    }

    static MinionSetting<Byte> ofByte(String name, byte defaultValue, String... comments) {
        return of(name, DataSerializers.BYTE, () -> defaultValue, comments);
    }

    static MinionSetting<Double> ofDouble(String name, double defaultValue, String... comments) {
        return of(name, DataSerializers.DOUBLE, () -> defaultValue, comments);
    }

    static MinionSetting<Float> ofFloat(String name, float defaultValue, String... comments) {
        return of(name, DataSerializers.FLOAT, () -> defaultValue, comments);
    }

    static MinionSetting<Character> ofCharacter(String name, char defaultValue, String... comments) {
        return of(name, DataSerializers.CHAR, () -> defaultValue, comments);
    }

    static MinionSetting<String> ofString(String name, String defaultValue, String... comments) {
        return of(name, DataSerializers.STRING, () -> defaultValue, comments);
    }

    static MinionSetting<List<String>> ofStringList(String name, List<String> defaultValue, String... comments) {
        return of(name, DataSerializers.ofList(DataSerializers.STRING), () -> new ArrayList<>(defaultValue), comments);
    }

    static <T extends Enum<T>> MinionSetting<T> ofEnum(String name, Class<T> enumClass, T defaultValue, String... comments) {
        return of(name, DataSerializers.ofEnum(enumClass), () -> defaultValue, comments);
    }

    static MinionSetting<ConfigurationSection> ofSection(String name, String... comments) {
        return ofValue(name, DataSerializers.SECTION, null, comments);
    }

    /**
     * Creates a RoseSetting with a given default value.
     * This should only be used for primitives or immutable types. Instances will be shared.
     *
     * @param name The name of the setting
     * @param serializer The serializer for the setting
     * @param defaultValue The default value, do not use a mutable value
     * @param comments Comments describing the setting for writing to YAML
     * @return a new RoseSetting
     * @param <T> the type of value this setting is for
     */
    static <T> MinionSetting<T> ofValue(String name, DataSerializer<T> serializer, T defaultValue, String... comments) {
        return new BasicMinionSetting<>(serializer, name.toLowerCase(), () -> defaultValue, false, comments != null ? comments : new String[0]);
    }

    /**
     * Creates a RoseSetting with a given default value supplier.
     *
     * @param name The name of the setting
     * @param serializer The serializer for the setting
     * @param defaultValueSupplier The default value supplier to return a new default value instance
     * @param comments Comments describing the setting for writing to YAML
     * @return a new RoseSetting
     * @param <T> the type of value this setting is for
     */
    static <T> MinionSetting<T> of(String name, DataSerializer<T> serializer, Supplier<T> defaultValueSupplier, String... comments) {
        return new BasicMinionSetting<>(serializer, name.toLowerCase(), defaultValueSupplier, false, comments != null ? comments : new String[0]);
    }

    /**
     * Creates a RoseSetting with a given default value.
     * This should only be used for primitives or immutable types. Instances will be shared.
     * Will not be written to YAML, will be written to PDC.
     *
     * @param name The name of the setting
     * @param serializer The serializer for the setting
     * @param defaultValue The default value, do not use a mutable value
     * @return a new RoseSetting
     * @param <T> the type of value this setting is for
     */
    static <T> MinionSetting<T> ofHiddenValue(String name, DataSerializer<T> serializer, T defaultValue) {
        return new BasicMinionSetting<>(serializer, name.toLowerCase(), () -> defaultValue, true);
    }

    /**
     * Creates a RoseSetting with a given default value supplier.
     * Will not be written to YAML, will be written to PDC.
     *
     * @param name The name of the setting
     * @param serializer The serializer for the setting
     * @param defaultValueSupplier The default value supplier to return a new default value instance
     * @return a new RoseSetting
     * @param <T> the type of value this setting is for
     */
    static <T> MinionSetting<T> ofHidden(String name, DataSerializer<T> serializer, Supplier<T> defaultValueSupplier) {
        return new BasicMinionSetting<>(serializer, name.toLowerCase(), defaultValueSupplier, true);
    }

}
