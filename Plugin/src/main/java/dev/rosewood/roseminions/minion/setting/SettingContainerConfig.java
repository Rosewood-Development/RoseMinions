package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Allows creating a {@link SettingContainer}
 */
public class SettingContainerConfig {

    private final Map<String, RoseSetting<?>> settings;
    private final Map<String, Supplier<?>> settingDefaultValueSuppliers;

    public SettingContainerConfig(SettingHolder settings, ConfigurationSection section) {
        this.settings = new LinkedHashMap<>();
        for (RoseSetting<?> setting : settings.get())
            this.settings.put(setting.getKey(), setting);
        this.settingDefaultValueSuppliers = new HashMap<>();

        if (section == null)
            return;

        for (RoseSetting<?> setting : this.settings.values())
            if (!setting.isHidden() && section.contains(setting.getKey()))
                this.settingDefaultValueSuppliers.put(setting.getKey(), () -> setting.read(section));
    }

    private SettingContainerConfig(SettingContainerConfig other) {
        this.settings = new LinkedHashMap<>(other.settings);
        this.settingDefaultValueSuppliers = new HashMap<>(other.settingDefaultValueSuppliers);
    }

    public Map<String, Supplier<?>> getSettingDefaultValueSuppliers() {
        return this.settingDefaultValueSuppliers;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(RoseSetting<T> setting) {
        Supplier<T> supplier = (Supplier<T>) this.settingDefaultValueSuppliers.get(setting.getKey());
        return supplier != null ? supplier.get() : setting.getDefaultValue();
    }

    public void merge(SettingContainerConfig other) {
        this.settingDefaultValueSuppliers.putAll(other.settingDefaultValueSuppliers);
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
