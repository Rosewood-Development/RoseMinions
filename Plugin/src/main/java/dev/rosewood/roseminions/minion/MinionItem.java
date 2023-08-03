package dev.rosewood.roseminions.minion;

import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;

import java.util.List;

public class MinionItem {

    private final SettingAccessor<String> displayName;
    private final SettingAccessor<List<String>> lore;
    private final SettingAccessor<String> texture;

    public MinionItem() {
        this.displayName = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING, "display-name", "&cMissing display-name");
        this.lore = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.ofList(SettingSerializers.STRING), "lore", List.of("", "<#c0ffee>Missing lore"));
        this.texture = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING, "texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=");
    }

    public SettingAccessor<String> getDisplayName() {
        return displayName;
    }

    public SettingAccessor<List<String>> getLore() {
        return lore;
    }

    public SettingAccessor<String> getTexture() {
        return texture;
    }

}
