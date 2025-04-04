package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;

public class SettingAccessor<T> {

    protected final SettingSerializer<T> serializer;
    protected final String key;
    private Supplier<T> defaultValueSupplier;
    protected final String[] comments;
    private final boolean hidden;

    protected SettingAccessor(SettingSerializer<T> serializer, String key, Supplier<T> defaultValueSupplier, boolean hidden, String... comments) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValueSupplier = defaultValueSupplier;
        this.hidden = hidden;
        this.comments = comments;
    }

    /**
     * Writes the setting and its default value to the given config.
     *
     * @param config the config to write to
     */
    public void write(CommentedConfigurationSection config) {
        if (this.hidden)
            return;

        T defaultValue = this.defaultValueSupplier.get();
        this.serializer.writeWithDefault(config, this.key, defaultValue, this.comments);
    }

    /**
     * Reads the setting from the given config and sets it as the default value supplier.
     *
     * @param config the config to read from
     */
    public void readDefault(ConfigurationSection config) {
        if (this.hidden || !config.contains(this.key))
            return;

        this.defaultValueSupplier = () -> this.serializer.read(config, this.key);
    }

    /**
     * Reads the setting from the given config.
     *
     * @param config the config to read from
     * @return the setting value
     */
    public T read(ConfigurationSection config) {
        if (this.hidden || !config.contains(this.key))
            return null;

        return this.serializer.read(config, this.key);
    }

    public SettingSerializer<T> getSerializer() {
        return this.serializer;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * @return a new instance of the default value
     */
    public T getDefaultValue() {
        return this.defaultValueSupplier.get();
    }

    /**
     * @return true if this setting should not be written as YAML, it will still be saved as PDC
     */
    public boolean isHidden() {
        return this.hidden;
    }

    /**
     * Creates a new SettingAccessor as a copy of this SettingAccessor but with a different default value.
     * This should only be used for primitives or immutable types. Instances will be shared.
     *
     * @param defaultValue the new default value
     * @return a copy of this SettingAccessor with a different default value
     */
    public SettingAccessor<T> copy(T defaultValue) {
        return new SettingAccessor<>(this.serializer, this.key, () -> defaultValue, false, this.comments);
    }

    /**
     * Creates a new SettingAccessor as a copy of this SettingAccessor but with a different default value.
     * This should be used for mutable types where the underlying value may be changed during the lifetime of
     * this accessor.
     *
     * @param defaultValueSupplier the new default value supplier
     * @return a copy of this SettingAccessor with a different default value
     */
    public SettingAccessor<T> copy(Supplier<T> defaultValueSupplier) {
        return new SettingAccessor<>(this.serializer, this.key, defaultValueSupplier, false, this.comments);
    }

    @Override
    public String toString() {
        return "SettingAccessor{" +
                "key='" + this.key + '\'' +
                ", defaultValue=" + this.defaultValueSupplier.get() +
                '}';
    }

    public static SettingAccessor<Boolean> defineBoolean(String name, boolean defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.BOOLEAN, () -> defaultValue, comments);
    }

    public static SettingAccessor<Integer> defineInteger(String name, int defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.INTEGER, () -> defaultValue, comments);
    }

    public static SettingAccessor<Long> defineLong(String name, long defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.LONG, () -> defaultValue, comments);
    }

    public static SettingAccessor<Short> defineShort(String name, short defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.SHORT, () -> defaultValue, comments);
    }

    public static SettingAccessor<Byte> defineByte(String name, byte defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.BYTE, () -> defaultValue, comments);
    }

    public static SettingAccessor<Double> defineDouble(String name, double defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.DOUBLE, () -> defaultValue, comments);
    }

    public static SettingAccessor<Float> defineFloat(String name, float defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.FLOAT, () -> defaultValue, comments);
    }

    public static SettingAccessor<Character> defineCharacter(String name, char defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.CHAR, () -> defaultValue, comments);
    }

    public static SettingAccessor<String> defineString(String name, String defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.STRING, () -> defaultValue, comments);
    }

    public static SettingAccessor<List<String>> defineStringList(String name, List<String> defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.ofList(SettingSerializers.STRING), () -> List.copyOf(defaultValue), comments);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> SettingAccessor<T> defineEnum(String name, T defaultValue, String... comments) {
        return defineSetting(name, SettingSerializers.ofEnum(defaultValue.getClass()), () -> defaultValue, comments);
    }

    public static <T> SettingAccessor<T> defineSetting(String name, SettingSerializer<T> serializer, Supplier<T> defaultValueSupplier, String... comments) {
        return new SettingAccessor<>(serializer, name.toLowerCase(), defaultValueSupplier, false, comments != null ? comments : new String[0]);
    }

    public static <T> SettingAccessor<T> defineHiddenSetting(String name, SettingSerializer<T> serializer, Supplier<T> defaultValueSupplier) {
        return new SettingAccessor<>(serializer, name.toLowerCase(), defaultValueSupplier, true);
    }

}
