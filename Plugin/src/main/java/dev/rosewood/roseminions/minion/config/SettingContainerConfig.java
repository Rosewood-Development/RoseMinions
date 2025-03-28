package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingContainer;
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

    private final Map<String, SettingAccessor<?>> settings;
    private final Map<String, Supplier<?>> settingDefaultValueSuppliers;

    public SettingContainerConfig(ModuleSettings settings, ConfigurationSection section) {
        this.settings = new LinkedHashMap<>();
        for (SettingAccessor<?> setting : settings.get())
            this.settings.put(setting.getKey(), setting);
        this.settingDefaultValueSuppliers = new HashMap<>();

        if (section == null)
            return;

        for (SettingAccessor<?> accessor : this.settings.values())
            if (!accessor.isHidden() && section.contains(accessor.getKey()))
                this.settingDefaultValueSuppliers.put(accessor.getKey(), () -> accessor.read(section));
    }

    private SettingContainerConfig(SettingContainerConfig other) {
        this.settings = new LinkedHashMap<>(other.settings);
        this.settingDefaultValueSuppliers = new HashMap<>(other.settingDefaultValueSuppliers);
    }

    public Map<String, Supplier<?>> getSettingDefaultValueSuppliers() {
        return this.settingDefaultValueSuppliers;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(SettingAccessor<T> accessor) {
        Supplier<T> supplier = (Supplier<T>) this.settingDefaultValueSuppliers.get(accessor.getKey());
        return supplier != null ? supplier.get() : accessor.getDefaultValue();
    }

    public void merge(SettingContainerConfig other) {
        this.settingDefaultValueSuppliers.putAll(other.settingDefaultValueSuppliers);
    }

    public SettingContainerConfig copy() {
        return new SettingContainerConfig(this);
    }

    @Override
    public String toString() {
        return "SettingsContainerConfig{" + this.settingDefaultValueSuppliers.entrySet().stream().map(x -> x.getKey() + "->" + x.getValue().get()).collect(Collectors.joining(",")) + "}";
    }

}
