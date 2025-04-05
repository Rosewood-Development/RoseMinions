package dev.rosewood.roseminions.config;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.roseminions.RoseMinions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static dev.rosewood.rosegarden.config.SettingSerializers.*;

public final class Settings implements SettingHolder {

    public static final SettingHolder INSTANCE = new Settings();
    private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

    public static final RoseSetting<List<String>> DISABLED_WORLDS = create("disabled-worlds", STRING_LIST, List.of("disabled_world_name"), "A list of worlds that the plugin is disabled in");
    public static final RoseSetting<Long> MINION_UPDATE_FREQUENCY = create("minion-update-frequency", LONG, 1L, "The number of ticks to wait between minion updates");

    private Settings() {}

    @Override
    public List<RoseSetting<?>> get() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private static <T> RoseSetting<T> create(String key, SettingSerializer<T> serializer, T defaultValue, String... comments) {
        RoseSetting<T> setting = RoseSetting.forBackedValue(key, RoseMinions.getInstance(), serializer, defaultValue, comments);
        SETTINGS.add(setting);
        return setting;
    }

}
