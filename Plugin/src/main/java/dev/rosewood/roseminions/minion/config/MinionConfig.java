package dev.rosewood.roseminions.minion.config;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a loaded configuration file for a minion
 */
public class MinionConfig {

    private final String id;
    private final List<RankConfig> ranks;

    public MinionConfig(CommentedFileConfiguration config) {
        this.ranks = new ArrayList<>();

        String id = config.getString("id");
        if (id == null)
            throw new IllegalArgumentException("Minion ID cannot be null");

        this.id = id.toLowerCase();

        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        if (ranksSection == null)
            throw new IllegalArgumentException("Minion " + this.id + " does not have any ranks");

        RankConfig previousRank = null;
        Set<String> rankKeys = ranksSection.getKeys(false);
        for (String rank : rankKeys) {
            ConfigurationSection rankSection = ranksSection.getConfigurationSection(rank);
            if (rankSection == null)
                throw new IllegalArgumentException("Minion " + this.id + " has an invalid rank: " + rank);

            previousRank = this.loadRank(previousRank, rank, rankSection);
            this.ranks.add(previousRank);
        }
    }

    private RankConfig loadRank(RankConfig previousRank, String rank, ConfigurationSection section) {
        Map<String, ModuleConfig> moduleData = new HashMap<>();

        // Load settings from previous rank
        SettingsContainerConfig previousRankItemSettings = null;
        if (previousRank != null) {
            previousRankItemSettings = previousRank.itemSettings().copy();
            previousRank.moduleData().forEach((module, data) -> moduleData.put(module, data.copy()));
        }

        ConfigurationSection itemSection = section.getConfigurationSection("item");
        SettingsContainerConfig itemSettings = new SettingsContainerConfig(MinionItem.INSTANCE, itemSection);

        if (previousRankItemSettings != null) {
            previousRankItemSettings.merge(itemSettings);
            itemSettings = previousRankItemSettings;
        }

        MinionModuleManager minionModuleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);
        ConfigurationSection modulesSection = section.getConfigurationSection("modules");
        this.getModules(modulesSection, minionModuleManager).values().forEach(data -> {
            ModuleConfig existingData = moduleData.get(data.id());
            if (existingData != null) {
                existingData.merge(data);
            } else {
                moduleData.put(data.id(), data);
            }
        });

        return new RankConfig(rank, itemSettings, moduleData);
    }

    private Map<String, ModuleConfig> getModules(ConfigurationSection section, MinionModuleManager minionModuleManager) {
        if (section == null)
            return new HashMap<>();

        Map<String, ModuleConfig> moduleData = new HashMap<>();
        for (String key : section.getKeys(false)) {
            key = key.toLowerCase();
            if (!minionModuleManager.isValidModule(key)) {
                RoseMinions.getInstance().getLogger().warning("Invalid module " + key + " for minion " + this.id);
                continue;
            }

            ConfigurationSection moduleSection = section.getConfigurationSection(key);
            if (moduleSection == null) {
                RoseMinions.getInstance().getLogger().warning("No settings found for module " + key + " for minion " + this.id);
                continue;
            }

            SettingsContainerConfig settingsContainer = minionModuleManager.getSectionSettings(key, moduleSection);
            Map<String, ModuleConfig> submodules = this.getModules(moduleSection.getConfigurationSection("sub-modules"), minionModuleManager);
            moduleData.put(key, new ModuleConfig(key, settingsContainer, submodules));
        }
        return moduleData;
    }

    public String getId() {
        return this.id;
    }

    public RankConfig getDefaultRank() {
        return this.ranks.getFirst();
    }

    public RankConfig getRank(String rank) {
        return this.ranks.stream().filter(x -> x.rank().equalsIgnoreCase(rank)).findFirst().orElse(null);
    }

    public List<RankConfig> getRanks() {
        return this.ranks;
    }

}
