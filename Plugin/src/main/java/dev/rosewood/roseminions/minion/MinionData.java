package dev.rosewood.roseminions.minion;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MinionData {

    private final String id;
    private final List<MinionRank> ranks;

    public MinionData(CommentedFileConfiguration config) {
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

    private MinionRank loadRank(int rank, ConfigurationSection section) {
        SettingsContainer itemSettings;
        Map<String, ModuleData> moduleData = new HashMap<>();

        // Load settings from previous rank
        if (rank > 0) {
            MinionRank previousRank = this.ranks.get(rank - 1);
            itemSettings = previousRank.getItemSettings().copy();
            previousRank.getModuleData().forEach((module, data) -> moduleData.put(module, data.copy()));
        } else {
            itemSettings = new SettingsContainer(MinionItem.class);
        }

        ConfigurationSection itemSection = section.getConfigurationSection("item");
        if (itemSection != null)
            itemSettings.loadDefaultsFromConfig(itemSection);

        MinionModuleManager minionModuleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);
        ConfigurationSection modulesSection = section.getConfigurationSection("modules");
        this.getModules(modulesSection, minionModuleManager).values().forEach(data -> {
            ModuleData existingData = moduleData.get(data.id());
            if (existingData != null) {
                existingData.merge(data);
            } else {
                moduleData.put(data.id(), data);
            }
        });

        return new MinionRank(rank, itemSettings, moduleData);
    }

    private Map<String, ModuleData> getModules(ConfigurationSection section, MinionModuleManager minionModuleManager) {
        if (section == null)
            return new HashMap<>();

        Map<String, ModuleData> moduleData = new HashMap<>();
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

            SettingsContainer settingsContainer = minionModuleManager.getSectionSettings(key, moduleSection);
            Map<String, ModuleData> submodules = this.getModules(moduleSection.getConfigurationSection("sub-modules"), minionModuleManager);
            moduleData.put(key, new ModuleData(key, settingsContainer, submodules));
        }
        return moduleData;
    }

    public String getId() {
        return this.id;
    }

    public int getMaxRank() {
        return this.ranks.size() - 1;
    }

    public MinionRank getRank(int rank) {
        if (rank < 0 || rank >= this.ranks.size())
            throw new IllegalArgumentException("Invalid rank " + rank + " for minion " + this.id);
        return this.ranks.get(rank);
    }

    public class MinionRank {

        private final int rank;
        private final SettingsContainer itemSettings;
        private final Map<String, ModuleData> moduleData;

        public MinionRank(int rank, SettingsContainer itemSettings, Map<String, ModuleData> moduleData) {
            this.rank = rank;
            this.itemSettings = itemSettings;
            this.moduleData = moduleData;

            // Ensure an "appearance" module always exists
            if (!this.moduleData.containsKey("appearance"))
                this.moduleData.put("appearance", new ModuleData("appearance", new SettingsContainer(AppearanceModule.class), new HashMap<>()));
        }

        public ItemStack getItemStack(boolean includeSettings) {
            StringPlaceholders placeholders = StringPlaceholders.builder("rank", this.rank).build();
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(HexUtils.colorify(placeholders.apply(this.itemSettings.get(MinionItem.DISPLAY_NAME))));
                itemMeta.setLore(this.itemSettings.get(MinionItem.LORE).stream().map(x -> HexUtils.colorify(placeholders.apply(x))).toList());
                SkullUtils.setSkullTexture(itemMeta, this.itemSettings.get(MinionItem.TEXTURE));

                if (includeSettings) {
                    PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
                    pdc.set(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING, MinionData.this.id);
                    pdc.set(MinionUtils.MINION_NEW_RANK_KEY, PersistentDataType.INTEGER, this.rank);
                }

                itemStack.setItemMeta(itemMeta);
            }

            return itemStack;
        }

        public SettingsContainer getItemSettings() {
            return this.itemSettings;
        }

        public Map<String, ModuleData> getModuleData() {
            return this.moduleData;
        }

    }

    public static class MinionItem {

        public static final SettingAccessor<String> DISPLAY_NAME;
        public static final SettingAccessor<List<String>> LORE;
        public static final SettingAccessor<String> TEXTURE;

        static {
            DISPLAY_NAME = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING, "display-name", "&cMissing display-name");
            LORE = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.ofList(SettingSerializers.STRING), "lore", List.of("", "<#c0ffee>Missing lore"));
            TEXTURE = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING, "texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=");
        }

    }

    public record ModuleData(String id, SettingsContainer settings, Map<String, ModuleData> subModules) {

        public ModuleData copy() {
            return new ModuleData(this.id, this.settings.copy(), this.copySubModules());
        }

        private Map<String, ModuleData> copySubModules() {
            Map<String, ModuleData> subModules = new HashMap<>();
            this.subModules.forEach((id, data) -> subModules.put(id, data.copy()));
            return subModules;
        }

        public void merge(ModuleData data) {
            this.settings.merge(data.settings);
            data.subModules().forEach((id, subData) -> {
                ModuleData existingData = this.subModules.get(id);
                if (existingData != null) {
                    existingData.merge(subData);
                } else {
                    this.subModules.put(id, subData.copy());
                }
            });
        }

    }

}
