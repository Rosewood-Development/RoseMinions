package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.RecordSettingSerializerBuilder;
import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import java.util.List;
import org.bukkit.Material;

public record ModuleGuiProperties(String title,
                                  Material icon,
                                  String iconName,
                                  List<String> iconLore) {

    public static final SettingSerializer<ModuleGuiProperties> SERIALIZER = RecordSettingSerializerBuilder.create(ModuleGuiProperties.class, instance -> instance.group(
            new SettingField<>("title", SettingSerializers.STRING, ModuleGuiProperties::title, "The title of the GUI"),
            new SettingField<>("icon", MinionSettingSerializers.MATERIAL, ModuleGuiProperties::icon, "The icon to use for this module in the minion GUI"),
            new SettingField<>("icon-name", SettingSerializers.STRING, ModuleGuiProperties::iconName, "The name to use for this module in the minion GUI"),
            new SettingField<>("icon-lore", SettingSerializers.ofList(SettingSerializers.STRING), ModuleGuiProperties::iconLore, "The lore to use for this module in the minion GUI")
    ).apply(instance, ModuleGuiProperties::new));

}
