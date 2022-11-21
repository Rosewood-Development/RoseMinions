package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.manager.AbstractConfigurationManager;
import dev.rosewood.roseminions.RoseMinions;
import java.util.Collections;

public class ConfigurationManager extends AbstractConfigurationManager {

    public enum Setting implements RoseSetting {
        DISABLED_WORLDS("disabled-worlds", Collections.singletonList("disabled_world_name"), "A list of worlds that the plugin is disabled in"),
        MINION_UPDATE_FREQUENCY("minion-update-frequency", 1, "The number of ticks to wait between minion updates");

        private final String key;
        private final Object defaultValue;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Object getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public String[] getComments() {
            return this.comments;
        }

        @Override
        public Object getCachedValue() {
            return this.value;
        }

        @Override
        public void setCachedValue(Object value) {
            this.value = value;
        }

        @Override
        public CommentedFileConfiguration getBaseConfig() {
            return RoseMinions.getInstance().getManager(ConfigurationManager.class).getConfig();
        }
    }

    public ConfigurationManager(RosePlugin rosePlugin) {
        super(rosePlugin, Setting.class);
    }

    @Override
    protected String[] getHeader() {
        return new String[] {
                "     __________                       _____   __        __",
                "     \\______   \\ ____  ______ ____   /     \\ |__| ____ |__| ____   ____   ______",
                "      |       _//  _ \\/  ___// __ \\ /  \\ /  \\|  |/    \\|  |/  _ \\ /    \\ /  ___/",
                "      |    |   (  <_> )___ \\\\  ___//    Y    \\  |   |  \\  (  <_> )   |  \\\\___ \\",
                "      |____|_  /\\____/____  >\\___  >____|__  /__|___|  /__|\\____/|___|  /____  >",
                "             \\/           \\/     \\/        \\/        \\/               \\/     \\/"
        };
    }

}
