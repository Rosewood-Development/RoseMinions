package dev.rosewood.roseminions.minion.config;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public record RankConfig(int rank, SettingsContainerConfig itemSettings, Map<String, ModuleConfig> moduleData) {

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
