package dev.rosewood.roseminions.minion;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.controller.AnimationController;
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

        RoseMinions.getInstance().getLogger().warning("Loaded minion type " + this.id);
    }

    private MinionRank loadRank(int rank, ConfigurationSection section) {
        SettingsContainer itemSettings;
        Map<String, SettingsContainer> modules;
        SettingsContainer animationSettings;

        // Load settings from previous rank
        if (rank > 0) {
            MinionRank previousRank = this.ranks.get(rank - 1);
            itemSettings = previousRank.getItemSettings().copy();
            modules = new HashMap<>();
            for (Map.Entry<String, SettingsContainer> entry : previousRank.getModuleSettings().entrySet())
                modules.put(entry.getKey(), entry.getValue().copy());
            previousRank.getModuleSettings().forEach((module, settings) -> modules.put(module, settings.copy()));
            animationSettings = previousRank.getAnimationSettings().copy();
        } else {
            itemSettings = new SettingsContainer(MinionItem.class);
            modules = new HashMap<>();
            animationSettings = new SettingsContainer(AnimationController.class);
        }

        ConfigurationSection itemSection = section.getConfigurationSection("item");
        if (itemSection != null)
            itemSettings.loadDefaultsFromConfig(itemSection);

        ConfigurationSection modulesSection = section.getConfigurationSection("modules");
        if (modulesSection != null) {
            MinionModuleManager minionModuleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);
            for (String key : modulesSection.getKeys(false)) {
                key = key.toLowerCase();
                if (!minionModuleManager.isValidModule(key)) {
                    RoseMinions.getInstance().getLogger().warning("Invalid module " + key + " for minion " + this.id);
                    continue;
                }

                ConfigurationSection moduleSection = modulesSection.getConfigurationSection(key);
                if (moduleSection == null) {
                    RoseMinions.getInstance().getLogger().warning("No settings found for module " + key + " for minion " + this.id);
                    continue;
                }

                SettingsContainer settingsContainer = minionModuleManager.getSectionSettings(key, moduleSection);
                modules.put(key, settingsContainer);
            }
        }

        ConfigurationSection animationsSection = section.getConfigurationSection("animation");
        if (animationsSection != null)
            animationSettings.loadDefaultsFromConfig(animationsSection);

        return new MinionRank(rank, itemSettings, modules, animationSettings);
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
        private final Map<String, SettingsContainer> moduleSettings;
        private final SettingsContainer animationSettings;

        public MinionRank(int rank, SettingsContainer itemSettings, Map<String, SettingsContainer> moduleSettings, SettingsContainer animationSettings) {
            this.rank = rank;
            this.itemSettings = itemSettings;
            this.moduleSettings = moduleSettings;
            this.animationSettings = animationSettings;
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

        public Map<String, SettingsContainer> getModuleSettings() {
            return this.moduleSettings;
        }

        public SettingsContainer getAnimationSettings() {
            return this.animationSettings;
        }

    }

    public static class MinionItem {

        public static final SettingAccessor<String> DISPLAY_NAME;
        public static final SettingAccessor<List<String>> LORE;
        public static final SettingAccessor<String> TEXTURE;

        static {
            DISPLAY_NAME = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING, "display-name", "&cMissing display-name");
            LORE = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING_LIST, "lore", List.of("", "<#c0ffee>Missing lore"));
            TEXTURE = SettingsContainer.defineSetting(MinionItem.class, SettingSerializers.STRING, "texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=");
        }

    }

}
