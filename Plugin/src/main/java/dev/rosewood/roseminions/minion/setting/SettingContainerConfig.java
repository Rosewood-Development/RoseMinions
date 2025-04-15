package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Allows creating a {@link SettingContainer}
 */
public class SettingContainerConfig {

    private final SettingHolder settings;
    private final Map<String, Supplier<?>> settingDefaultValueSuppliers;

    public SettingContainerConfig(SettingHolder settings, ConfigurationSection section) {
        this.settings = settings;
        this.settingDefaultValueSuppliers = new HashMap<>();

        if (section == null)
            return;

        for (RoseSetting<?> setting : this.settings.get())
            if (!setting.isHidden() && setting.readIsValid(section))
                this.settingDefaultValueSuppliers.put(setting.getKey(), () -> setting.read(section));
    }

    private SettingContainerConfig(SettingContainerConfig other) {
        this.settings = other.settings;
        this.settingDefaultValueSuppliers = new HashMap<>(other.settingDefaultValueSuppliers);
    }

    SettingHolder getSettings() {
        return this.settings;
    }

    @SuppressWarnings("unchecked")
    <T> SettingValue<T> createValue(RoseSetting<T> setting) {
        Supplier<T> supplier = (Supplier<T>) this.settingDefaultValueSuppliers.get(setting.getKey());
        return supplier != null ? new SettingValue<>(setting, supplier.get()) : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(RoseSetting<T> setting) {
        Supplier<T> supplier = (Supplier<T>) this.settingDefaultValueSuppliers.get(setting.getKey());
        return supplier != null ? supplier.get() : setting.getDefaultValue();
    }

    public void merge(SettingContainerConfig other) {
        if (!other.settings.equals(this.settings))
            throw new IllegalArgumentException("Cannot merge SettingContainerConfigs that do not have the same settings");
        this.settingDefaultValueSuppliers.putAll(other.settingDefaultValueSuppliers); // TODO: Merge instead of overwriting
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
