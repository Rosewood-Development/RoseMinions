package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.minion.config.SettingsContainerConfig;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SettingsContainer implements DataSerializable {

    private final Class<?> clazz;
    private final Map<String, SettingItem<?>> settings;

    public SettingsContainer(Class<?> clazz) {
        this.clazz = clazz;
        this.settings = new HashMap<>();

        this.loadDefaults(this.clazz);
    }

    public SettingsContainer(SettingsContainerConfig config) {
        this(config.getSettingsClass());

        config.getSettingDefaultValueSuppliers().forEach((key, supplier) -> {
            SettingItem<?> settingItem = this.settings.get(key);
            if (settingItem != null) {
                SettingItem<?> newSettingItem = new SettingItem<>(settingItem.getAccessor(), MinionUtils.forceCast(supplier.get()));
                this.settings.put(key, newSettingItem);
            }
        });
    }

    public <T> void loadDefaults(Class<T> clazz) {
        for (SettingAccessor<?> accessor : SettingsRegistry.REGISTERED_SETTINGS.get(clazz))
            this.loadDefault(accessor);
    }

    public <T> void loadDefault(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = new SettingItem<>(accessor, accessor.getDefaultValue());
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
            // Only write settings that have changed from their default values
            Map<String, SettingItem<?>> changedSettings = this.settings.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().isModified())
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
                inputStream.readFully(itemBytes);
                SettingItem<?> item = this.getItem(key);
                if (item != null)
                    item.deserialize(itemBytes);
            }
        });
    }

    public void setDefaults(SettingsContainerConfig settings) {
        Map<String, Supplier<?>> defaultSettingSuppliers = settings.getSettingDefaultValueSuppliers();
        SettingsRegistry.REGISTERED_SETTINGS.get(this.clazz).forEach(settingAccessor -> {
            String key = settingAccessor.getKey();
            Supplier<?> supplier = defaultSettingSuppliers.get(key);
            if (supplier == null)
                return;

            SettingItem<?> newSettingItem = new SettingItem<>(settingAccessor, MinionUtils.forceCast(supplier.get()));
            this.settings.put(key, newSettingItem);
        });
    }

}
