package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.roseminions.model.PDCSerializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

public class SettingContainer implements PDCSerializable {

    private final SettingHolder settings;
    private final Map<String, SettingValue<?>> settingValues;

    public SettingContainer(SettingHolder settings) {
        this.settings = settings;
        this.settingValues = new HashMap<>();
        this.loadDefaults();
    }

    private void loadDefaults() {
        for (RoseSetting<?> setting : this.settings.get())
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
                .filter(entry -> entry.getValue().shouldPersist())
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

    public void loadConfig(SettingContainerConfig settingsConfig) {
        if (!this.settings.equals(settingsConfig.getSettings()))
            throw new IllegalArgumentException("Cannot load SettingContainerConfig that does not have the same settings as this SettingContainer");

        for (RoseSetting<?> setting : this.settings.get()) {
            String key = setting.getKey();
            SettingValue<?> settingValue = settingsConfig.createValue(setting);
            if (settingValue != null)
                this.settingValues.put(key, settingValue); // TODO: Merge instead of overwrite
        }
    }

}
