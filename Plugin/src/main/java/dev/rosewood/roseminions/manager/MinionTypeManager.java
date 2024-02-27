package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MinionTypeManager extends Manager {

    private static final String DIRECTORY = "minions";

    private final Map<String, MinionConfig> minionTypes;

    public MinionTypeManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.minionTypes = new HashMap<>();
    }

    @Override
    public void reload() {
        File directory = new File(this.rosePlugin.getDataFolder(), DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();

            // Save default minion types
            this.rosePlugin.saveResource("minions/slayer.yml", false);
        }

        // Load minion types
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (!file.getName().endsWith(".yml"))
                continue;

            try {
                CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);
                MinionConfig minionConfig = new MinionConfig(config);
                this.minionTypes.put(minionConfig.getId(), minionConfig);
            } catch (Exception e) {
                this.rosePlugin.getLogger().warning("Failed to load minion type from file " + file.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disable() {
        this.minionTypes.clear();
    }

    public MinionConfig getMinionData(String id) {
        return this.minionTypes.get(id.toLowerCase());
    }

    public Collection<String> getMinionTypes() {
        return Collections.unmodifiableCollection(this.minionTypes.keySet());
    }

}
