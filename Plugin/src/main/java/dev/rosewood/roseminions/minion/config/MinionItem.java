package dev.rosewood.roseminions.minion.config;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinionItem implements SettingHolder {

    public static final MinionItem INSTANCE = new MinionItem();
    private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

    public static final RoseSetting<String> DISPLAY_NAME = define(RoseSetting.forString("display-name", "&cMissing display-name"));
    public static final RoseSetting<List<String>> LORE = define(RoseSetting.forStringList("lore", List.of("", "<#c0ffee>Missing lore")));
    public static final RoseSetting<String> TEXTURE = define(RoseSetting.forString("texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0="));

    private MinionItem() { }

    @Override
    public List<RoseSetting<?>> get() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private static <T> RoseSetting<T> define(RoseSetting<T> setting) {
        SETTINGS.add(setting);
        return setting;
    }

}
