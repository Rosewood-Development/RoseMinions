package dev.rosewood.roseminions.minion.config;

import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinionItem implements ModuleSettings {

    public static final MinionItem INSTANCE = new MinionItem();
    private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

    public static final SettingAccessor<String> DISPLAY_NAME = define(SettingAccessor.defineString("display-name", "&cMissing display-name"));
    public static final SettingAccessor<List<String>> LORE = define(SettingAccessor.defineStringList("lore", List.of("", "<#c0ffee>Missing lore")));
    public static final SettingAccessor<String> TEXTURE = define(SettingAccessor.defineString("texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0="));

    private MinionItem() { }

    @Override
    public List<SettingAccessor<?>> get() {
        return Collections.unmodifiableList(ACCESSORS);
    }

    private static <T> SettingAccessor<T> define(SettingAccessor<T> accessor) {
        ACCESSORS.add(accessor);
        return accessor;
    }

}
