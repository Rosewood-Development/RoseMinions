package dev.rosewood.roseminions.minion;


import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class MinionRank {

    private final int rank;
    private final MinionData minionData;
    private final SettingsContainer itemSettings;
    private final Map<String, ModuleData> moduleData;

    public MinionRank(int rank, MinionData minionData, SettingsContainer itemSettings, Map<String, ModuleData> moduleData) {
        this.rank = rank;
        this.minionData = minionData;
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
            itemMeta.setDisplayName(HexUtils.colorify(placeholders.apply(this.itemSettings.get(this.minionData.getItem().getDisplayName()))));
            itemMeta.setLore(this.itemSettings.get(this.minionData.getItem().getLore()).stream().map(x -> HexUtils.colorify(placeholders.apply(x))).toList());
            SkullUtils.setSkullTexture(itemMeta, this.itemSettings.get(this.minionData.getItem().getTexture()));

            if (includeSettings) {
                PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
                pdc.set(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING, this.minionData.getId());
                pdc.set(MinionUtils.MINION_NEW_RANK_KEY, PersistentDataType.INTEGER, this.rank);
            }

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public int getRank() {
        return rank;
    }

    public MinionData getMinionData() {
        return minionData;
    }

    public SettingsContainer getItemSettings() {
        return this.itemSettings;
    }

    public Map<String, ModuleData> getModuleData() {
        return this.moduleData;
    }

}