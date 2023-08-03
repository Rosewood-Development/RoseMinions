package dev.rosewood.roseminions.minion;


import dev.rosewood.roseminions.minion.setting.SettingsContainer;

import java.util.HashMap;
import java.util.Map;

public record ModuleData(String id, SettingsContainer settings, Map<String, ModuleData> subModules) {

    public ModuleData copy() {
        return new ModuleData(this.id, this.settings.copy(), this.copySubModules());
    }

    private Map<String, ModuleData> copySubModules() {
        Map<String, ModuleData> subModules = new HashMap<>();
        this.subModules.forEach((id, data) -> subModules.put(id, data.copy()));
        return subModules;
    }

    public void merge(ModuleData data) {
        this.settings.merge(data.settings);
        data.subModules().forEach((id, subData) -> {
            ModuleData existingData = this.subModules.get(id);
            if (existingData != null) {
                existingData.merge(subData);
            } else {
                this.subModules.put(id, subData.copy());
            }
        });
    }

}