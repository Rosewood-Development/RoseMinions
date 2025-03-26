package dev.rosewood.roseminions.config;

import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.RoseSettingSerializer;
import dev.rosewood.roseminions.RoseMinions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.*;

public final class SettingKey {

    private static final List<RoseSetting<?>> KEYS = new ArrayList<>();

    public static final RoseSetting<List<String>> DISABLED_WORLDS = create("disabled-worlds", STRING_LIST, List.of("disabled_world_name"), "A list of worlds that the plugin is disabled in");
    public static final RoseSetting<Long> MINION_UPDATE_FREQUENCY = create("minion-update-frequency", LONG, 1L, "The number of ticks to wait between minion updates");

    private static <T> RoseSetting<T> create(String key, RoseSettingSerializer<T> serializer, T defaultValue, String... comments) {
        RoseSetting<T> setting = RoseSetting.backed(RoseMinions.getInstance(), key, serializer, defaultValue, comments);
        KEYS.add(setting);
        return setting;
    }

    private static RoseSetting<CommentedConfigurationSection> create(String key, String... comments) {
        RoseSetting<CommentedConfigurationSection> setting = RoseSetting.backedSection(RoseMinions.getInstance(), key, comments);
        KEYS.add(setting);
        return setting;
    }

    public static List<RoseSetting<?>> getKeys() {
        return Collections.unmodifiableList(KEYS);
    }

    private SettingKey() {}

}
