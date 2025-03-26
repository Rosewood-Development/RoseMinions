package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import java.util.List;

public interface ModuleSettings {

    List<SettingAccessor<?>> get();

}
