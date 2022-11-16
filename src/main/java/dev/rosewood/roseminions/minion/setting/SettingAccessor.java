package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import java.util.Arrays;
import org.bukkit.configuration.ConfigurationSection;

public class SettingAccessor<T> {

    private final SettingSerializer<T> serializer;
    private final String key;
    private final T defaultValue;
    private final String[] comments;
    private T loadedConfigValue;

    public SettingAccessor(SettingSerializer<T> serializer, String key, T defaultValue, String... comments) {
        this.serializer = serializer;
        this.key = key;
        this.defaultValue = defaultValue;
        this.comments = comments;
    }

    /**
     * Writes the setting and its default value to the given config.
     * Adds the default setting value to the end of the comments.
     *
     * @param config the config to write to
     */
    public void write(CommentedFileConfiguration config) {
        String[] comments = Arrays.copyOf(this.comments, this.comments.length + 1);
        comments[comments.length - 1] = "Default: " + this.defaultValue;
        this.serializer.write(config, this.key, this.defaultValue, comments);
    }

    /**
     * Reads the setting from the given config and sets it as the default value.
     *
     * @param config the config to read from
     */
    public void readDefault(ConfigurationSection config) {
        this.loadedConfigValue = this.serializer.read(config, this.key);
    }

    /**
     * Reads the setting from the given config.
     *
     * @param config the config to read from
     * @return the setting value
     */
    public T read(ConfigurationSection config) {
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

}
