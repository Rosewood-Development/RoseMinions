package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.minion.config.SettingsContainerConfig;
import dev.rosewood.roseminions.model.PDCSerializable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;

public class SettingsContainer implements PDCSerializable {

    private final Map<String, SettingAccessor<?>> settings;
    private final Map<String, SettingItem<?>> settingValues;

    public SettingsContainer(List<SettingAccessor<?>> settings) {
        this.settings = new LinkedHashMap<>();
        for (SettingAccessor<?> setting : settings)
            this.settings.put(setting.getKey(), setting);
        this.settingValues = new HashMap<>();
        this.loadDefaults();
    }

    public void loadDefaults() {
        for (SettingAccessor<?> accessor : this.settings.values())
            this.loadDefault(accessor);
    }

    public <T> void loadDefault(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = new SettingItem<>(accessor, accessor.getDefaultValue());
        this.settingValues.put(accessor.getKey(), settingItem);
    }

    public <T> T get(SettingAccessor<T> accessor) {
        SettingItem<T> settingItem = this.getItem(accessor);
        if (settingItem == null)
            throw new IllegalArgumentException("SettingsContainer does not have " + accessor.getKey() + " defined");
        return settingItem.getValue();
    }

    public <T> void set(SettingAccessor<T> accessor, T value) {
        SettingItem<T> settingItem = this.getItem(accessor);
        if (settingItem == null)
            throw new IllegalArgumentException("SettingsContainer for does not have " + accessor.getKey() + " defined");
        settingItem.setValue(value);
    }

    private <T> SettingItem<T> getItem(SettingAccessor<T> accessor) {
        return this.getItem(accessor.getKey());
    }

    @SuppressWarnings("unchecked")
    private <T> SettingItem<T> getItem(String key) {
        return (SettingItem<T>) this.settingValues.get(key);
    }

    @Override
    public void writePDC(PersistentDataContainer container, PersistentDataAdapterContext context) {
        // Only write settings that have changed from their default values
        Map<String, SettingItem<?>> changedSettings = this.settingValues.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isModified())
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);

        for (Map.Entry<String, SettingItem<?>> entry : changedSettings.entrySet())
            entry.getValue().writePDC(container, context);
    }

    @Override
    public void readPDC(PersistentDataContainer container) {
        for (NamespacedKey settingKey : container.getKeys()) {
            String key = settingKey.getKey();
            SettingItem<?> item = this.getItem(key);
            if (item != null)
                item.readPDC(container);
        }
    }

    public void setDefaults(SettingsContainerConfig settings) {
        Map<String, Supplier<?>> defaultSettingSuppliers = settings.getSettingDefaultValueSuppliers();
        for (SettingAccessor<?> setting : this.settings.values()) {
            String key = setting.getKey();
            Supplier<?> supplier = defaultSettingSuppliers.get(key);
            if (supplier == null)
                continue;

            SettingItem<?> newSettingItem = new SettingItem<>(setting, MinionUtils.forceCast(supplier.get()));
            this.settingValues.put(key, newSettingItem);
        }
    }

}
