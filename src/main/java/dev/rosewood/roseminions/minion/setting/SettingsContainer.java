package dev.rosewood.roseminions.minion.setting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.roseminions.model.ObjectSerializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SettingsContainer implements ObjectSerializable {

    public static final Multimap<Class<?>, DefaultSettingItem<?>> REGISTERED_SETTINGS = MultimapBuilder.hashKeys().arrayListValues().build();

    private final Map<String, SettingItem<?>> settings;

    public SettingsContainer() {
        this.settings = new HashMap<>();
    }

    public static <T> SettingAccessor<T> defineSetting(Class<?> clazz, SettingSerializer<T> serializer, String name, T defaultValue, String... comments) {
        String key = name.toLowerCase();
        DefaultSettingItem<T> settingItem = new DefaultSettingItem<>(serializer, key, defaultValue, comments);
        if (REGISTERED_SETTINGS.containsEntry(clazz, settingItem))
            throw new IllegalArgumentException("Setting " + settingItem.key() + " is already defined for class " + clazz.getSimpleName());

        REGISTERED_SETTINGS.put(clazz, settingItem);
        return createAccessor(settingItem);
    }

    private static <T> SettingAccessor<T> createAccessor(DefaultSettingItem<T> settingItem) {
        return new SettingAccessor<>(settingItem);
    }

    public void loadDefaults(Class<?> clazz) {
        for (DefaultSettingItem<?> settingItem : REGISTERED_SETTINGS.get(clazz))
            this.loadDefault(new SettingAccessor<>(settingItem));
    }

    public <T> void loadDefault(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = new SettingItem<>(accessor.getSerializer(), accessor.getDefaultValue());
        this.settings.put(accessor.getKey(), settingItem);
    }

    public <T> T get(SettingAccessor<T> accessor) {
        return this.getItem(accessor).getValue();
    }

    public <T> void set(SettingAccessor<T> accessor, T value) {
        this.getItem(accessor).setValue(value);
    }

    public void merge(SettingsContainer other) {
        for (Map.Entry<String, SettingItem<?>> entry : other.settings.entrySet()) {
            SettingItem<?> item = entry.getValue();
            if (this.settings.containsKey(entry.getKey()))
                this.settings.put(entry.getKey(), item);
        }
    }

    private <T> SettingItem<T> getItem(SettingAccessor<T> accessor) {
        return this.getItem(accessor.getKey());
    }

    @SuppressWarnings("unchecked")
    private <T> SettingItem<T> getItem(String key) {
        return (SettingItem<T>) this.settings.get(key);
    }

    @Override
    public void serialize(ObjectOutputStream outputStream) throws IOException {
        int length = this.settings.size();
        outputStream.writeInt(length);
        for (Map.Entry<String, SettingItem<?>> entry : this.settings.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            entry.getValue().serialize(outputStream);
        }
    }

    @Override
    public void deserialize(ObjectInputStream inputStream) throws IOException {
        int length = inputStream.readInt();
        for (int i = 0; i < length; i++) {
            String key = inputStream.readUTF();
            SettingItem<?> item = this.getItem(key);
            item.deserialize(inputStream);
        }
    }

    public record DefaultSettingItem<T>(SettingSerializer<T> serializer, String key, T defaultValue, String... comments) {

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

    }

    public static class SettingItem<T> implements ObjectSerializable {

        private final SettingSerializer<T> serializer;
        private T value;

        public SettingItem(SettingSerializer<T> serializer, T value) {
            this.serializer = serializer;
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public void serialize(ObjectOutputStream outputStream) throws IOException {
            this.serializer.write(outputStream, this.value);
        }

        @Override
        public void deserialize(ObjectInputStream inputStream) throws IOException {
            this.value = this.serializer.read(inputStream);
        }

    }

}
