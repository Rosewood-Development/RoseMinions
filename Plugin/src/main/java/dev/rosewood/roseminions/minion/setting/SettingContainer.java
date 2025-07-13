package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.roseminions.object.PDCSerializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

public class SettingContainer implements PDCSerializable {

    private final PDCSettingHolder settings;
    private final Map<String, SettingValue<?>> settingValues;

    public SettingContainer(PDCSettingHolder settings) {
        this.settings = settings;
        this.settingValues = new HashMap<>();
        for (PDCRoseSetting<?> setting : settings.get())
            this.settingValues.put(setting.getKey(), new SettingValue<>(setting, null));
    }

    public <T> T get(PDCRoseSetting<T> setting) {
        SettingValue<T> settingValue = this.getItem(setting);
        if (settingValue == null)
            throw new IllegalArgumentException("SettingsContainer does not have " + setting.getKey() + " defined");
        return settingValue.getValue();
    }

    public <T> void set(PDCRoseSetting<T> setting, T value) {
        SettingValue<T> settingValue = this.getItem(setting);
        if (settingValue == null)
            throw new IllegalArgumentException("SettingsContainer for does not have " + setting.getKey() + " defined");
        settingValue.setValue(value);
    }

    private <T> SettingValue<T> getItem(PDCRoseSetting<T> setting) {
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

    public void loadConfig(SettingContainerConfig settingConfig) {
        if (!this.settings.equals(settingConfig.getSettings()))
            throw new IllegalArgumentException("Cannot load SettingContainerConfig that does not have the same settings as this SettingContainer");

        for (PDCRoseSetting<?> setting : this.settings.get())
            this.loadSetting(setting, settingConfig);
    }

    @SuppressWarnings("unchecked")
    private <T> void loadSetting(PDCRoseSetting<T> setting, SettingContainerConfig settingConfig) {
        SettingValue<T> settingValue = (SettingValue<T>) this.settingValues.get(setting.getKey());
        if (settingValue == null)
            throw new IllegalArgumentException("SettingValue was expected to exist, but did not");
        T value = settingConfig.get(setting);
        if (value != null)
            settingValue.changeValue(value);
    }

}
