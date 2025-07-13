package dev.rosewood.roseminions.minion.config;

import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinionItem implements PDCSettingHolder {

    public static final MinionItem INSTANCE = new MinionItem();
    private static final List<PDCRoseSetting<?>> SETTINGS = new ArrayList<>();

    public static final PDCRoseSetting<String> DISPLAY_NAME = define(PDCRoseSetting.ofString("display-name", "&cMissing display-name"));
    public static final PDCRoseSetting<List<String>> LORE = define(PDCRoseSetting.ofStringList("lore", List.of("", "<#c0ffee>Missing lore")));
    public static final PDCRoseSetting<String> TEXTURE = define(PDCRoseSetting.ofString("texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0="));

    private MinionItem() { }

    @Override
    public List<PDCRoseSetting<?>> get() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private static <T> PDCRoseSetting<T> define(PDCRoseSetting<T> setting) {
        SETTINGS.add(setting);
        return setting;
    }

}
