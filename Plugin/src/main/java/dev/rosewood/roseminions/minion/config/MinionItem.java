package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import java.util.List;

public final class MinionItem {

    public static final SettingAccessor<String> DISPLAY_NAME;
    public static final SettingAccessor<List<String>> LORE;
    public static final SettingAccessor<String> TEXTURE;

    static {
        DISPLAY_NAME = SettingsRegistry.defineString(MinionItem.class, "display-name", "&cMissing display-name");
        LORE = SettingsRegistry.defineStringList(MinionItem.class, "lore", List.of("", "<#c0ffee>Missing lore"));
        TEXTURE = SettingsRegistry.defineString(MinionItem.class, "texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=");
    }

    private MinionItem() {

    }

}
