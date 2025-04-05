package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.SettingContainerConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a loaded configuration file for a minion module
 *
 * @param id The ID of the module
 * @param settings The default settings for the module
 * @param subModules The submodules of the module
 */
public record ModuleConfig(String id, SettingContainerConfig settings, Map<String, ModuleConfig> subModules) {

    public ModuleConfig copy() {
        return new ModuleConfig(this.id, this.settings.copy(), this.copySubModules());
    }

    private Map<String, ModuleConfig> copySubModules() {
        Map<String, ModuleConfig> subModules = new HashMap<>();
        this.subModules.forEach((id, data) -> subModules.put(id, data.copy()));
        return subModules;
    }

    public void merge(ModuleConfig data) {
        this.settings.merge(data.settings);
        data.subModules().forEach((id, subData) -> {
            ModuleConfig existingData = this.subModules.get(id);
            if (existingData != null) {
                existingData.merge(subData);
            } else {
                this.subModules.put(id, subData.copy());
            }
        });
    }

}
