package dev.rosewood.roseminions.minion.config;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.setting.SettingContainerConfig;
import dev.rosewood.roseminions.util.SkullUtils;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public record RankConfig(String rank, SettingContainerConfig itemSettings, Map<String, ModuleConfig> moduleData) {

    public ItemStack getDisplayItemStack() {
        StringPlaceholders placeholders = StringPlaceholders.builder("rank", this.rank).build();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        if (itemStack.getItemMeta() instanceof SkullMeta itemMeta) {
            itemMeta.setDisplayName(HexUtils.colorify(placeholders.apply(this.itemSettings.get(MinionItem.DISPLAY_NAME))));
            itemMeta.setLore(this.itemSettings.get(MinionItem.LORE).stream().map(x -> HexUtils.colorify(placeholders.apply(x))).toList());
            SkullUtils.setSkullTexture(itemMeta, this.itemSettings.get(MinionItem.TEXTURE));
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

}
