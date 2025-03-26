package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;

public class SettingAccessor<T> {

    protected final SettingSerializer<T> serializer;
    protected final String key;
    private Supplier<T> defaultValueSupplier;
    protected final String[] comments;
    private final boolean hidden;

    protected SettingAccessor(SettingSerializer<T> serializer, String key, Supplier<T> defaultValueSupplier, String... comments) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValueSupplier = defaultValueSupplier;
        this.comments = comments;
        this.hidden = false;
    }

    protected SettingAccessor(SettingSerializer<T> serializer, String key, Supplier<T> defaultValueSupplier) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValueSupplier = defaultValueSupplier;
        this.comments = new String[0];
        this.hidden = true;
    }

    /**
     * Writes the setting and its default value to the given config.
     * Adds the default setting value to the end of the comments.
     *
     * @param config the config to write to
     */
    public void write(CommentedConfigurationSection config) {
        if (this.hidden)
            return;

        T defaultValue = this.defaultValueSupplier.get();
        if (this.serializer.isStringificationAllowed() && defaultValue != null) {
            String[] comments = Arrays.copyOf(this.comments, this.comments.length + 1);
            comments[comments.length - 1] = "Default: " + this.serializer.stringify(defaultValue);
            this.serializer.write(config, this.key, defaultValue, comments);
        } else {
            this.serializer.write(config, this.key, defaultValue, this.comments);
        }
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
     * @return true if this setting should not be written to the config
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
        return new SettingAccessor<>(this.serializer, this.key, () -> defaultValue, this.comments);
    }

    @Override
    public String toString() {
        return "SettingAccessor{" +
                "key='" + key + '\'' +
                ", defaultValueSupplier=" + defaultValueSupplier.get() +
                '}';
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
        return new SettingAccessor<>(this.serializer, this.key, defaultValueSupplier, this.comments);
    }

    public static SettingAccessor<Boolean> defineBoolean(String name, boolean defaultValue, String... comments) {
        return defineSetting(SettingSerializers.BOOLEAN, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Integer> defineInteger(String name, int defaultValue, String... comments) {
        return defineSetting(SettingSerializers.INTEGER, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Long> defineLong(String name, long defaultValue, String... comments) {
        return defineSetting(SettingSerializers.LONG, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Short> defineShort(String name, short defaultValue, String... comments) {
        return defineSetting(SettingSerializers.SHORT, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Byte> defineByte(String name, byte defaultValue, String... comments) {
        return defineSetting(SettingSerializers.BYTE, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Double> defineDouble(String name, double defaultValue, String... comments) {
        return defineSetting(SettingSerializers.DOUBLE, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Float> defineFloat(String name, float defaultValue, String... comments) {
        return defineSetting(SettingSerializers.FLOAT, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<Character> defineCharacter(String name, char defaultValue, String... comments) {
        return defineSetting(SettingSerializers.CHAR, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<String> defineString(String name, String defaultValue, String... comments) {
        return defineSetting(SettingSerializers.STRING, name, () -> defaultValue, comments);
    }

    public static SettingAccessor<List<String>> defineStringList(String name, List<String> defaultValue, String... comments) {
        return defineSetting(SettingSerializers.ofList(SettingSerializers.STRING), name, () -> List.copyOf(defaultValue), comments);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> SettingAccessor<T> defineEnum(String name, T defaultValue, String... comments) {
        return defineSetting(SettingSerializers.ofEnum(defaultValue.getClass()), name, () -> defaultValue, comments);
    }

    public static <T> SettingAccessor<T> defineSetting(SettingSerializer<T> serializer, String name, Supplier<T> defaultValueSupplier, String... comments) {
        String key = name.toLowerCase();
        if (comments == null) {
            return new SettingAccessor<>(serializer, key, defaultValueSupplier);
        } else {
            return new SettingAccessor<>(serializer, key, defaultValueSupplier, comments);
        }
    }

    public static <T> SettingAccessor<T> defineHiddenSetting(SettingSerializer<T> serializer, String name, Supplier<T> defaultValueSupplier) {
        return defineSetting(serializer, name, defaultValueSupplier, (String[]) null);
    }

}
