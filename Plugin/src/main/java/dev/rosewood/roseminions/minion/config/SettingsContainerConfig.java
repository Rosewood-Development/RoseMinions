package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Allows creating a {@link SettingsContainer}
 */
public class SettingsContainerConfig {

    private final Class<?> clazz;
    private final Map<String, Supplier<?>> settingDefaultValueSuppliers;

    public SettingsContainerConfig(Class<?> clazz, ConfigurationSection section) {
        this.clazz = clazz;
        this.settingDefaultValueSuppliers = new HashMap<>();

        if (section == null)
            return;

        for (SettingAccessor<?> accessor : SettingsRegistry.REGISTERED_SETTINGS.get(this.clazz))
            if (!accessor.isHidden() && section.contains(accessor.getKey()))
                this.settingDefaultValueSuppliers.put(accessor.getKey(), () -> accessor.read(section));
    }

    private SettingsContainerConfig(SettingsContainerConfig other) {
        this.clazz = other.clazz;
        this.settingDefaultValueSuppliers = new HashMap<>(other.settingDefaultValueSuppliers);
    }

    public Class<?> getSettingsClass() {
        return this.clazz;
    }

    public Map<String, Supplier<?>> getSettingDefaultValueSuppliers() {
        return this.settingDefaultValueSuppliers;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(SettingAccessor<T> accessor) {
        Supplier<T> supplier = (Supplier<T>) this.settingDefaultValueSuppliers.get(accessor.getKey());
        return supplier != null ? supplier.get() : accessor.getDefaultValue();
    }

    public void merge(SettingsContainerConfig other) {
        this.settingDefaultValueSuppliers.putAll(other.getSettingDefaultValueSuppliers());
    }

    public SettingsContainerConfig copy() {
        return new SettingsContainerConfig(this);
    }

}
