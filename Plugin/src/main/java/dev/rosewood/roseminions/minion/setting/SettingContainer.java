package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.roseminions.model.PDCSerializable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

public class SettingContainer implements PDCSerializable {

    private final Map<String, RoseSetting<?>> settings;
    private final Map<String, SettingValue<?>> settingValues;

    public SettingContainer(List<RoseSetting<?>> settings) {
        this.settings = new LinkedHashMap<>();
        for (RoseSetting<?> setting : settings)
            this.settings.put(setting.getKey(), setting);
        this.settingValues = new HashMap<>();
        this.loadDefaults();
    }

    private void loadDefaults() {
        for (RoseSetting<?> setting : this.settings.values())
            this.loadDefault(setting);
    }

    private <T> void loadDefault(RoseSetting<T> setting) {
        SettingValue<T> settingValue = new SettingValue<>(setting, setting.getDefaultValue());
        this.settingValues.put(setting.getKey(), settingValue);
    }

    public <T> T get(RoseSetting<T> setting) {
        SettingValue<T> settingValue = this.getItem(setting);
        if (settingValue == null)
            throw new IllegalArgumentException("SettingsContainer does not have " + setting.getKey() + " defined");
        return settingValue.getValue();
    }

    public <T> void set(RoseSetting<T> setting, T value) {
        SettingValue<T> settingValue = this.getItem(setting);
        if (settingValue == null)
            throw new IllegalArgumentException("SettingsContainer for does not have " + setting.getKey() + " defined");
        settingValue.setValue(value);
    }

    private <T> SettingValue<T> getItem(RoseSetting<T> setting) {
        return this.getItem(setting.getKey());
    }

    @SuppressWarnings("unchecked")
    private <T> SettingValue<T> getItem(String key) {
        return (SettingValue<T>) this.settingValues.get(key);
    }

    @Override
    public void writePDC(PersistentDataContainer container) {
        // Only write settings that have changed from their default values
        Map<String, SettingValue<?>> changedSettings = this.settingValues.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isModified())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, SettingValue<?>> entry : changedSettings.entrySet())
            entry.getValue().writePDC(container);
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

    public void loadConfig(SettingContainerConfig settings) {
        Map<String, Supplier<?>> defaultSettingSuppliers = settings.getSettingDefaultValueSuppliers();
        for (RoseSetting<?> setting : this.settings.values()) {
            String key = setting.getKey();
            Supplier<?> supplier = defaultSettingSuppliers.get(key);
            if (supplier == null)
                continue;

            SettingValue<?> newSettingValue = new SettingValue<>(setting, MinionUtils.forceCast(supplier.get()));
            this.settingValues.put(key, newSettingValue);
        }
    }

}
