package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import java.util.List;
import org.bukkit.Material;

public record ModuleGuiProperties(String title,
                                  Material icon,
                                  String iconName,
                                  List<String> iconLore) implements Mergeable<ModuleGuiProperties> {

    public static final SettingSerializer<ModuleGuiProperties> SERIALIZER = SettingSerializers.ofRecord(ModuleGuiProperties.class, instance -> instance.group(
            SettingField.ofOptionalValue("title", SettingSerializers.STRING, ModuleGuiProperties::title, null, "The title of the GUI"),
            SettingField.ofOptionalValue("icon", SettingSerializers.MATERIAL, ModuleGuiProperties::icon, null, "The icon to use for this module in the minion GUI"),
            SettingField.ofOptionalValue("icon-name", SettingSerializers.STRING, ModuleGuiProperties::iconName, null, "The name to use for this module in the minion GUI"),
            SettingField.ofOptionalValue("icon-lore", SettingSerializers.ofList(SettingSerializers.STRING), ModuleGuiProperties::iconLore, null, "The lore to use for this module in the minion GUI")
    ).apply(instance, ModuleGuiProperties::new));

    @Override
    public ModuleGuiProperties merge(ModuleGuiProperties other) {
        return new ModuleGuiProperties(
                Mergeable.merge(this.title, other.title),
                Mergeable.merge(this.icon, other.icon),
                Mergeable.merge(this.iconName, other.iconName),
                Mergeable.merge(this.iconLore, other.iconLore)
        );
    }

}
