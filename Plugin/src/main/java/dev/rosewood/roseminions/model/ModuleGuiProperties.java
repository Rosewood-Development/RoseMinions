package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import java.util.List;
import org.bukkit.Material;

public record ModuleGuiProperties(String title,
                                  Material icon,
                                  String iconName,
                                  List<String> iconLore) {

    public static final SettingSerializer<ModuleGuiProperties> SERIALIZER = SettingSerializers.ofRecord(ModuleGuiProperties.class, instance -> instance.group(
            SettingField.of("title", SettingSerializers.STRING, ModuleGuiProperties::title, "The title of the GUI"),
            SettingField.of("icon", SettingSerializers.MATERIAL, ModuleGuiProperties::icon, "The icon to use for this module in the minion GUI"),
            SettingField.of("icon-name", SettingSerializers.STRING, ModuleGuiProperties::iconName, "The name to use for this module in the minion GUI"),
            SettingField.of("icon-lore", SettingSerializers.ofList(SettingSerializers.STRING), ModuleGuiProperties::iconLore, "The lore to use for this module in the minion GUI")
    ).apply(instance, ModuleGuiProperties::new));

}
