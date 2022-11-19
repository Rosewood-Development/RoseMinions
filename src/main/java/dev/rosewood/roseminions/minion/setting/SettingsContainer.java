package dev.rosewood.roseminions.minion.setting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.roseminions.model.DataSerializable;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class SettingsContainer implements DataSerializable {

    public static final Multimap<Class<?>, SettingAccessor<?>> REGISTERED_SETTINGS = MultimapBuilder.hashKeys().arrayListValues().build();

    private final Class<?> clazz;
    private final Map<String, SettingItem<?>> settings;

    public SettingsContainer(Class<?> clazz) {
        this.clazz = clazz;
        this.settings = new HashMap<>();

        this.loadDefaults(this.clazz);
    }

    public static <T> SettingAccessor<T> defineSetting(Class<?> clazz, SettingSerializer<T> serializer, String name, T defaultValue, String... comments) {
        String key = name.toLowerCase();
        SettingAccessor<T> accessor = new SettingAccessor<>(serializer, key, defaultValue, comments);
        if (REGISTERED_SETTINGS.containsEntry(clazz, accessor))
            throw new IllegalArgumentException("Setting " + accessor.getKey() + " is already defined for class " + clazz.getSimpleName());

        REGISTERED_SETTINGS.put(clazz, accessor);
        return accessor;
    }

    public static <T> SettingAccessor<T> defineHiddenSetting(Class<?> clazz, SettingSerializer<T> serializer, String name, T defaultValue) {
        String key = name.toLowerCase();
        SettingAccessor<T> accessor = new SettingAccessor<>(serializer, key, defaultValue);
        if (REGISTERED_SETTINGS.containsEntry(clazz, accessor))
            throw new IllegalArgumentException("Setting " + accessor.getKey() + " is already defined for class " + clazz.getSimpleName());

        REGISTERED_SETTINGS.put(clazz, accessor);
        return accessor;
    }

    public static <C, T> void redefineSetting(Class<C> clazz, SettingAccessor<T> accessor, T defaultValue) {
        REGISTERED_SETTINGS.put(clazz, accessor.copy(defaultValue));
    }

    public <T> void loadDefaults(Class<T> clazz) {
        for (SettingAccessor<?> accessor : REGISTERED_SETTINGS.get(clazz))
            this.loadDefault(accessor);
    }

    public <T> void loadDefault(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = new SettingItem<>(accessor.getSerializer(), accessor.getDefaultValue());
        this.settings.put(accessor.getKey(), settingItem);
    }

    public <T> T get(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = this.getItem(accessor);
        if (settingItem == null)
            throw new IllegalArgumentException("SettingsContainer for " + this.clazz.getSimpleName() + " does not have " + accessor.getKey() + " defined");
        return settingItem.getValue();
    }

    public <T> void set(SettingAccessor<T> accessor, T value) {
        SettingItem<T> settingItem = this.getItem(accessor);
        if (settingItem == null)
            throw new IllegalArgumentException("SettingsContainer for " + this.clazz.getSimpleName() + " does not have " + accessor.getKey() + " defined");
        settingItem.setValue(value);
    }

    public <T> void loadDefaultsFromConfig(ConfigurationSection section) {
        for (SettingAccessor<?> accessor : REGISTERED_SETTINGS.get(this.clazz)) {
            @SuppressWarnings("unchecked")
            SettingAccessor<T> typedAccessor = (SettingAccessor<T>) accessor;
            if (!typedAccessor.isHidden() && section.contains(typedAccessor.getKey())) {
                SettingItem<T> settingItem = new SettingItem<>(typedAccessor.getSerializer(), typedAccessor.read(section));
                this.settings.put(accessor.getKey(), settingItem);
            }
        }
    }

    public void merge(SettingsContainer other) {
        for (Map.Entry<String, SettingItem<?>> entry : other.settings.entrySet()) {
            SettingItem<?> item = entry.getValue();
            if (this.settings.containsKey(entry.getKey()))
                this.settings.put(entry.getKey(), item.copy());
        }
    }

    private <T> SettingItem<T> getItem(SettingAccessor<T> accessor) {
        return this.getItem(accessor.getKey());
    }

    @SuppressWarnings("unchecked")
    private <T> SettingItem<T> getItem(String key) {
        return (SettingItem<T>) this.settings.get(key);
    }

    public SettingsContainer copy() {
        SettingsContainer copy = new SettingsContainer(this.clazz);
        copy.merge(this);
        return copy;
    }

    @Override
    public byte[] serialize() {
        return DataSerializable.write(outputStream -> {
            // Only write settings that have changed from their default values
            Map<String, SettingItem<?>> changedSettings = this.settings.entrySet()
                    .stream()
                    .filter(entry -> !entry.getValue().isDefaultValue())
                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
            outputStream.writeInt(changedSettings.size());
            for (Map.Entry<String, SettingItem<?>> entry : changedSettings.entrySet()) {
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
        private boolean modified;

        public SettingItem(SettingSerializer<T> serializer, T value) {
            this.serializer = serializer;
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T value) {
            this.value = value;
            this.modified = true;
        }

        public SettingItem<T> copy() {
            return new SettingItem<>(this.serializer, this.value);
        }

        public boolean isDefaultValue() {
            return !this.modified;
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
