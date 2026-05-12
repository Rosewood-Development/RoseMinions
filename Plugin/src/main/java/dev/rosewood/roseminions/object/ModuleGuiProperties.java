package dev.rosewood.roseminions.object;

import dev.rosewood.roseminions.setting.DataSerializer;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.SettingField;
import java.util.List;
import org.bukkit.Material;

public record ModuleGuiProperties(String title,
                                  Material icon,
                                  String iconName,
                                  List<String> iconLore) implements Mergeable<ModuleGuiProperties> {

    public static final DataSerializer<ModuleGuiProperties> SERIALIZER = DataSerializers.ofRecord(ModuleGuiProperties.class, instance -> instance.group(
            SettingField.ofOptionalValue("title", DataSerializers.STRING, ModuleGuiProperties::title, null, "The title of the GUI"),
            SettingField.ofOptionalValue("icon", DataSerializers.MATERIAL, ModuleGuiProperties::icon, null, "The icon to use for this module in the minion GUI"),
            SettingField.ofOptionalValue("icon-name", DataSerializers.STRING, ModuleGuiProperties::iconName, null, "The name to use for this module in the minion GUI"),
            SettingField.ofOptionalValue("icon-lore", DataSerializers.STRING_LIST, ModuleGuiProperties::iconLore, null, "The lore to use for this module in the minion GUI")
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
