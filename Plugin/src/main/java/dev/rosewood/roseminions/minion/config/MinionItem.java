package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.setting.MinionSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinionItem implements PDCSettingHolder {

    public static final MinionItem INSTANCE = new MinionItem();
    private static final List<MinionSetting<?>> SETTINGS = new ArrayList<>();

    public static final MinionSetting<String> DISPLAY_NAME = define(MinionSetting.ofString("display-name", "&cMissing display-name"));
    public static final MinionSetting<List<String>> LORE = define(MinionSetting.ofStringList("lore", List.of("", "<#c0ffee>Missing lore")));
    public static final MinionSetting<String> TEXTURE = define(MinionSetting.ofString("texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0="));

    private MinionItem() { }

    @Override
    public List<MinionSetting<?>> get() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private static <T> MinionSetting<T> define(MinionSetting<T> setting) {
        SETTINGS.add(setting);
        return setting;
    }

}
