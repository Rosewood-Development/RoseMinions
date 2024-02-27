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

        try {
            Set<String> rankKeys = ranksSection.getKeys(false);
            int[] rankIds = rankKeys.stream().mapToInt(Integer::parseInt).sorted().toArray();
            if (rankIds.length == 0)
                throw new IllegalArgumentException("Minion " + this.id + " does not have any ranks");

            if (rankIds[rankIds.length - 1] != rankIds.length - 1)
                throw new IllegalArgumentException("Minion " + this.id + " is missing a rank number, must have " + rankIds.length + " total ranks starting at 0");

            for (int rank : rankIds) {
                ConfigurationSection rankSection = ranksSection.getConfigurationSection(String.valueOf(rank));
                if (rankSection == null)
                    throw new IllegalArgumentException("Minion " + this.id + " has an invalid rank number: " + rank);

                this.ranks.add(rank, this.loadRank(rank, rankSection));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Minion " + this.id + " has an invalid rank number");
        }
    }

    private RankConfig loadRank(int rank, ConfigurationSection section) {
        Map<String, ModuleConfig> moduleData = new HashMap<>();

        // Load settings from previous rank
        SettingsContainerConfig previousRankItemSettings = null;
        if (rank > 0) {
            RankConfig previousRank = this.ranks.get(rank - 1);
            previousRankItemSettings = previousRank.itemSettings().copy();
            previousRank.moduleData().forEach((module, data) -> moduleData.put(module, data.copy()));
        }

        ConfigurationSection itemSection = section.getConfigurationSection("item");
        SettingsContainerConfig itemSettings = new SettingsContainerConfig(MinionItem.class, itemSection);

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

    public int getMaxRank() {
        return this.ranks.size() - 1;
    }

    public RankConfig getRank(int rank) {
        if (rank < 0 || rank >= this.ranks.size())
            throw new IllegalArgumentException("Invalid rank " + rank + " for minion " + this.id);
        return this.ranks.get(rank);
    }

}
