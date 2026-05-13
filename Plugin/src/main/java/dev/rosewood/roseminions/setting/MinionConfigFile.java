package dev.rosewood.roseminions.setting;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public interface MinionConfigFile {

    List<MinionSetting<?>> getSettings();

    <T> T get(MinionSetting<T> setting);

    default <T> T get(String key, DataSerializer<T> serializer) {
        return this.get(key, serializer, (T) null);
    }

    default <T> T get(String key, DataSerializer<T> serializer, T defaultValue) {
        return this.get(MinionSetting.ofValue(key, serializer, defaultValue));
    }

    default <T> T get(String key, DataSerializer<T> serializer, Supplier<T> defaultValueSupplier) {
        return this.get(MinionSetting.of(key, serializer, defaultValueSupplier));
    }

    <T> void set(MinionSetting<T> setting, T value);

    default <T> void set(String key, DataSerializer<T> serializer, T value) {
        this.set(MinionSetting.ofValue(key, serializer, value), value);
    }

    default <T> void set(String key, DataSerializer<T> serializer, Supplier<T> valueSupplier) {
        this.set(MinionSetting.of(key, serializer, valueSupplier), valueSupplier.get());
    }

    File getFile();

    CommentedFileConfiguration getBaseConfig();

    void reload();

    default void save() {
        this.getBaseConfig().save(this.getFile());
    }

    static Builder builder(File file) {
        return new BasicMinionConfigFile.Builder(file);
    }

    interface Builder {

        Builder header(String... header);

        Builder settings(List<MinionSetting<?>> settings);

        Builder settings(MinionSettingHolder settingHolder);

        Builder writeDefaultValueComments();

        MinionConfigFile build();

    }

}
