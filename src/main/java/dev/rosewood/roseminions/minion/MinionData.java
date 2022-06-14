package dev.rosewood.roseminions.minion;

import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;

public class MinionData {

    private final List<MinionRank> ranks;

    public MinionData(FileConfiguration config) {
        this.ranks = new ArrayList<>();
    }

    public static class MinionRank {

        private final Map<Class<? extends MinionModule>, SettingsContainer> modules;

        public MinionRank(FileConfiguration config) {
            this.modules = new HashMap<>();
        }

    }

}
