package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Allows creating a {@link SettingContainer}
 */
public class SettingContainerConfig {

    private final SettingHolder settings;
    private final Map<String, ValueCombinator<?>> settingDefaultValueSuppliers;

    public SettingContainerConfig(SettingHolder settings, ConfigurationSection section) {
        this.settings = settings;
        this.settingDefaultValueSuppliers = new HashMap<>();

        if (section == null)
            return;

        for (RoseSetting<?> setting : this.settings.get())
            if (setting.readIsValid(section))
                this.settingDefaultValueSuppliers.put(setting.getKey(), new ValueCombinator<>(() -> setting.read(section)));
    }

    private SettingContainerConfig(SettingContainerConfig other) {
        this.settings = other.settings;
        this.settingDefaultValueSuppliers = other.settingDefaultValueSuppliers.entrySet().stream()
                .map(x -> Map.entry(x.getKey(), new ValueCombinator<>(x.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
    }

    SettingHolder getSettings() {
        return this.settings;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(RoseSetting<T> setting) {
        ValueCombinator<T> valueCombinator = (ValueCombinator<T>) this.settingDefaultValueSuppliers.get(setting.getKey());
        if (valueCombinator != null) {
            T value = valueCombinator.get();
            if (value != null)
                return value;
        }
        return setting.getDefaultValue();
    }

    public void merge(SettingContainerConfig other) {
        if (!other.settings.equals(this.settings))
            throw new IllegalArgumentException("Cannot merge SettingContainerConfigs that do not have the same settings");
        for (Map.Entry<String, ValueCombinator<?>> entry : other.settingDefaultValueSuppliers.entrySet())
            this.merge(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    private <T> void merge(String key, ValueCombinator<T> otherValueCombinator) {
        ValueCombinator<T> existing = (ValueCombinator<T>) this.settingDefaultValueSuppliers.get(key);
        if (existing != null) {
            existing.combine(otherValueCombinator);
        } else {
            this.settingDefaultValueSuppliers.put(key, otherValueCombinator);
        }
    }

    public SettingContainerConfig copy() {
        return new SettingContainerConfig(this);
    }

    @Override
    public String toString() {
        return "SettingsContainerConfig{" +
                this.settingDefaultValueSuppliers.entrySet().stream()
                        .map(x -> x.getKey() + "->" + x.getValue().get())
                        .collect(Collectors.joining(",")) + "}";
    }

}
