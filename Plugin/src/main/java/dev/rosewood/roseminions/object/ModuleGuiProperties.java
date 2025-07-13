package dev.rosewood.roseminions.object;

import dev.rosewood.rosegarden.config.PDCSettingField;
import dev.rosewood.rosegarden.config.PDCSettingSerializer;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
import java.util.List;
import org.bukkit.Material;

public record ModuleGuiProperties(String title,
                                  Material icon,
                                  String iconName,
                                  List<String> iconLore) implements Mergeable<ModuleGuiProperties> {

    public static final PDCSettingSerializer<ModuleGuiProperties> SERIALIZER = PDCSettingSerializers.ofRecord(ModuleGuiProperties.class, instance -> instance.group(
            PDCSettingField.ofOptionalValue("title", PDCSettingSerializers.STRING, ModuleGuiProperties::title, null, "The title of the GUI"),
            PDCSettingField.ofOptionalValue("icon", PDCSettingSerializers.MATERIAL, ModuleGuiProperties::icon, null, "The icon to use for this module in the minion GUI"),
            PDCSettingField.ofOptionalValue("icon-name", PDCSettingSerializers.STRING, ModuleGuiProperties::iconName, null, "The name to use for this module in the minion GUI"),
            PDCSettingField.ofOptionalValue("icon-lore", PDCSettingSerializers.STRING_LIST, ModuleGuiProperties::iconLore, null, "The lore to use for this module in the minion GUI")
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
