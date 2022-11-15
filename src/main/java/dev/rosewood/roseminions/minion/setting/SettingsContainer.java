package dev.rosewood.roseminions.minion.setting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.roseminions.model.DataSerializable;
import java.util.HashMap;
import java.util.Map;

public class SettingsContainer implements DataSerializable {

    public static final Multimap<Class<?>, SettingAccessor<?>> REGISTERED_SETTINGS = MultimapBuilder.hashKeys().arrayListValues().build();

    private final Map<String, SettingItem<?>> settings;

    public SettingsContainer() {
        this.settings = new HashMap<>();
    }

    public static <T> SettingAccessor<T> defineSetting(Class<?> clazz, SettingSerializer<T> serializer, String name, T defaultValue, String... comments) {
        String key = name.toLowerCase();
        SettingAccessor<T> accessor = new SettingAccessor<>(serializer, key, defaultValue, comments);
        if (REGISTERED_SETTINGS.containsEntry(clazz, accessor))
            throw new IllegalArgumentException("Setting " + accessor.getKey() + " is already defined for class " + clazz.getSimpleName());

        REGISTERED_SETTINGS.put(clazz, accessor);
        return accessor;
    }

    public void loadDefaults(Class<?> clazz) {
        for (SettingAccessor<?> accessor : REGISTERED_SETTINGS.get(clazz))
            this.loadDefault(accessor);
    }

    public <T> void loadDefault(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = new SettingItem<>(accessor.getSerializer(), accessor.getValue());
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
    public byte[] serialize() {
        return DataSerializable.write(outputStream -> {
            int length = this.settings.size();
            outputStream.writeInt(length);
            for (Map.Entry<String, SettingItem<?>> entry : this.settings.entrySet()) {
                outputStream.writeUTF(entry.getKey());
                byte[] itemBytes = entry.getValue().serialize();
                outputStream.writeInt(itemBytes.length);
                outputStream.write(itemBytes);
            }
        });
    }

    @Override
    public void deserialize(byte[] input) {
        DataSerializable.read(input, inputStream -> {
            int length = inputStream.readInt();
            for (int i = 0; i < length; i++) {
                String key = inputStream.readUTF();
                int itemLength = inputStream.readInt();
                byte[] itemBytes = new byte[itemLength];
                inputStream.read(itemBytes);
                SettingItem<?> item = this.getItem(key);
                if (item != null)
                    item.deserialize(itemBytes);
            }
        });
    }

    public static class SettingItem<T> implements DataSerializable {

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
        public byte[] serialize() {
            return DataSerializable.write(outputStream -> {
                byte[] valueBytes = this.serializer.write(this.value);
                outputStream.writeInt(valueBytes.length);
                outputStream.write(valueBytes);
            });
        }

        @Override
        public void deserialize(byte[] input) {
            DataSerializable.read(input, inputStream -> {
                int valueLength = inputStream.readInt();
                byte[] valueBytes = new byte[valueLength];
                inputStream.read(valueBytes);
                this.value = this.serializer.read(valueBytes);
            });
        }

    }

}
