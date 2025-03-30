package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.setting.Field;
import dev.rosewood.roseminions.minion.setting.RecordSettingSerializerBuilder;
import dev.rosewood.roseminions.minion.setting.SettingSerializer;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.List;
import org.bukkit.Material;

public record ModuleGuiProperties(String title,
                                  Material icon,
                                  String iconName,
                                  List<String> iconLore) {

    public static final SettingSerializer<ModuleGuiProperties> SERIALIZER = RecordSettingSerializerBuilder.create(ModuleGuiProperties.class, instance -> instance.group(
            new Field<>("title", SettingSerializers.STRING, ModuleGuiProperties::title, "The title of the GUI"),
            new Field<>("icon", SettingSerializers.MATERIAL, ModuleGuiProperties::icon, "The icon to use for this module in the minion GUI"),
            new Field<>("icon-name", SettingSerializers.STRING, ModuleGuiProperties::iconName, "The name to use for this module in the minion GUI"),
            new Field<>("icon-lore", SettingSerializers.ofList(SettingSerializers.STRING), ModuleGuiProperties::iconLore, "The lore to use for this module in the minion GUI")
    ).apply(instance, ModuleGuiProperties::new));

}
