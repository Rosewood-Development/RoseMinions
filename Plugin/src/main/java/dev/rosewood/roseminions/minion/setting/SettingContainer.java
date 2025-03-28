package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.minion.config.SettingContainerConfig;
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

public class SettingContainer implements PDCSerializable {

    private final Map<String, SettingAccessor<?>> settings;
    private final Map<String, SettingValue<?>> settingValues;

    public SettingContainer(List<SettingAccessor<?>> settings) {
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
        SettingValue<T> settingValue = new SettingValue<>(accessor, accessor.getDefaultValue());
        this.settingValues.put(accessor.getKey(), settingValue);
    }

    public <T> T get(SettingAccessor<T> accessor) {
        SettingValue<T> settingValue = this.getItem(accessor);
        if (settingValue == null)
            throw new IllegalArgumentException("SettingsContainer does not have " + accessor.getKey() + " defined");
        return settingValue.getValue();
    }

    public <T> void set(SettingAccessor<T> accessor, T value) {
        SettingValue<T> settingValue = this.getItem(accessor);
        if (settingValue == null)
            throw new IllegalArgumentException("SettingsContainer for does not have " + accessor.getKey() + " defined");
        settingValue.setValue(value);
    }

    private <T> SettingValue<T> getItem(SettingAccessor<T> accessor) {
        return this.getItem(accessor.getKey());
    }

    @SuppressWarnings("unchecked")
    private <T> SettingValue<T> getItem(String key) {
        return (SettingValue<T>) this.settingValues.get(key);
    }

    @Override
    public void writePDC(PersistentDataContainer container, PersistentDataAdapterContext context) {
        // Only write settings that have changed from their default values
        Map<String, SettingValue<?>> changedSettings = this.settingValues.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isModified())
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);

        for (Map.Entry<String, SettingValue<?>> entry : changedSettings.entrySet())
            entry.getValue().writePDC(container, context);
    }

    @Override
    public void readPDC(PersistentDataContainer container) {
        for (NamespacedKey settingKey : container.getKeys()) {
            String key = settingKey.getKey();
            SettingValue<?> item = this.getItem(key);
            if (item != null)
                item.readPDC(container);
        }
    }

    public void setDefaults(SettingContainerConfig settings) {
        Map<String, Supplier<?>> defaultSettingSuppliers = settings.getSettingDefaultValueSuppliers();
        for (SettingAccessor<?> setting : this.settings.values()) {
            String key = setting.getKey();
            Supplier<?> supplier = defaultSettingSuppliers.get(key);
            if (supplier == null)
                continue;

            SettingValue<?> newSettingValue = new SettingValue<>(setting, MinionUtils.forceCast(supplier.get()));
            this.settingValues.put(key, newSettingValue);
        }
    }

}
