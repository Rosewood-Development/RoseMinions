package dev.rosewood.roseminions.minion;

import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionAnimationManager;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MinionData {

    private final String id;
    private final ItemStack itemStack;
    private final List<MinionRank> ranks;

    public MinionData(CommentedFileConfiguration config) {
        this.ranks = new ArrayList<>();
        this.itemStack = new ItemStack(Material.PLAYER_HEAD);

        String id = config.getString("id");
        if (id == null)
            throw new IllegalArgumentException("Minion ID cannot be null");

        this.id = id.toLowerCase();

        // Load default rank (0)
        this.ranks.add(this.loadRank(config));

        // TODO: Load additional ranks

        // Load item settings
        ConfigurationSection itemSection = config.getConfigurationSection("item");
        if (itemSection != null) {
            SkullMeta itemMeta = (SkullMeta) this.itemStack.getItemMeta();
            if (itemMeta != null) {
                String displayName = itemSection.getString("display-name");
                if (displayName != null)
                    itemMeta.setDisplayName(displayName);

                itemMeta.setLore(itemSection.getStringList("lore"));

                String texture = itemSection.getString("texture");
                if (texture != null)
                    SkullUtils.setSkullTexture(itemMeta, texture);

                this.itemStack.setItemMeta(itemMeta);
            }
        }

        if (this.ranks.isEmpty())
            throw new IllegalArgumentException("Minion must have at least one rank");

        RoseMinions.getInstance().getLogger().warning("Loaded minion type " + this.id);
    }

    private MinionRank loadRank(ConfigurationSection section) {
        Map<String, SettingsContainer> modules = new HashMap<>();
        MinionModuleManager minionModuleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);

        ConfigurationSection modulesSection = section.getConfigurationSection("modules");
        if (modulesSection != null) {
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

        Map<String, SettingsContainer> animations = new HashMap<>();
        MinionAnimationManager minionAnimationManager = RoseMinions.getInstance().getManager(MinionAnimationManager.class);

        ConfigurationSection animationsSection = section.getConfigurationSection("animations");
        if (animationsSection != null) {
            for (String key : animationsSection.getKeys(false)) {
                key = key.toLowerCase();
                if (!minionAnimationManager.isValidAnimation(key)) {
                    RoseMinions.getInstance().getLogger().warning("Invalid animation " + key + " for minion " + this.id);
                    continue;
                }

                ConfigurationSection animationSection = animationsSection.getConfigurationSection(key);
                if (animationSection == null) {
                    RoseMinions.getInstance().getLogger().warning("No settings found for animation " + key + " for minion " + this.id);
                    continue;
                }

                SettingsContainer settingsContainer = minionAnimationManager.getSectionSettings(key, animationSection);
                animations.put(key, settingsContainer);
            }
        }

        return new MinionRank(modules, animations);
    }

    public String getId() {
        return this.id;
    }

    public int getMaxRank() {
        return this.ranks.size() - 1;
    }

    public ItemStack getItemStack(int rank) {
        MinionRank minionRank = this.ranks.get(rank);
        if (minionRank == null)
            throw new IllegalArgumentException("Invalid rank " + rank + " for minion " + this.id);

        // Apply coloring and placeholders
        StringPlaceholders placeholders = StringPlaceholders.single("rank", rank);
        ItemStack itemStack = this.itemStack.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasDisplayName())
                itemMeta.setDisplayName(HexUtils.colorify(placeholders.apply(itemMeta.getDisplayName())));

            List<String> lore = itemMeta.getLore();
            if (lore != null)
                itemMeta.setLore(lore.stream().map(x -> HexUtils.colorify(placeholders.apply(x))).toList());

            PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
            pdc.set(MinionUtils.MINION_NEW_KEY, PersistentDataType.STRING, this.id);

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public MinionRank getRank(int rank) {
        return this.ranks.get(rank);
    }

    public record MinionRank(Map<String, SettingsContainer> modules, Map<String, SettingsContainer> animations) {

    }

}
