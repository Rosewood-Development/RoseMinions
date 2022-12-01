package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.Arrays;
import org.bukkit.configuration.ConfigurationSection;

public class SettingAccessor<T> {

    protected final SettingSerializer<T> serializer;
    protected final String key;
    private final T defaultValue;
    protected final String[] comments;
    private T loadedConfigValue;
    private final boolean hidden;

    protected SettingAccessor(SettingSerializer<T> serializer, String key, T defaultValue, String... comments) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValue = defaultValue;
        this.comments = comments;
        this.hidden = false;
    }

    protected SettingAccessor(SettingSerializer<T> serializer, String key, T defaultValue) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValue = defaultValue;
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

        if (this.serializer.isStringificationAllowed() && this.defaultValue != null) {
            String[] comments = Arrays.copyOf(this.comments, this.comments.length + 1);
            comments[comments.length - 1] = "Default: " + this.serializer.stringify(this.defaultValue);
            this.serializer.write(config, this.key, this.defaultValue, comments);
        } else {
            this.serializer.write(config, this.key, this.defaultValue, this.comments);
        }
    }

    /**
     * Reads the setting from the given config and sets it as the default value.
     *
     * @param config the config to read from
     */
    public void readDefault(ConfigurationSection config) {
        if (this.hidden && !config.contains(this.key))
            return;

        this.loadedConfigValue = this.serializer.read(config, this.key);
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

    public T getDefaultValue() {
        return this.loadedConfigValue == null ? this.defaultValue : this.loadedConfigValue;
    }

    public SettingAccessor<T> copy(T defaultValue) {
        return new SettingAccessor<>(this.serializer, this.key, defaultValue != null ? defaultValue : this.defaultValue, this.comments);
    }

    public boolean isHidden() {
        return this.hidden;
    }

}
