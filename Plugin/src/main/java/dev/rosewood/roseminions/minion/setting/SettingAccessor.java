package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.Arrays;
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

    public SettingAccessor<T> copy(Supplier<T> defaultValueSupplier) {
        return new SettingAccessor<>(this.serializer, this.key, defaultValueSupplier, this.comments);
    }

    public boolean isHidden() {
        return this.hidden;
    }

}
