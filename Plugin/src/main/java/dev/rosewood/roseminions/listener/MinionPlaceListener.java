package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MinionPlaceListener implements Listener {

    private final RosePlugin rosePlugin;

    public MinionPlaceListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMinionPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.PLAYER_HEAD && event.getBlockPlaced().getType() != Material.PLAYER_WALL_HEAD)
            return;

        ItemStack itemStack = event.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        if (pdc.has(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);

            String minionId = pdc.get(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING);
            if (minionId == null)
                return;

            Integer minionRank = pdc.get(MinionUtils.MINION_NEW_RANK_KEY, PersistentDataType.INTEGER);
            if (minionRank == null)
                return;

            MinionData minionData = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(minionId);
            if (minionData == null) {
                event.getPlayer().sendMessage("Invalid minion ID: " + minionId);
                return;
            }

            if (minionRank < 0 || minionRank > minionData.getMaxRank()) {
                event.getPlayer().sendMessage("Invalid minion rank: " + minionRank);
                return;
            }

            Minion minion = new Minion(minionData, minionRank, event.getPlayer().getUniqueId(), event.getBlockPlaced().getLocation(), false);
            minionManager.registerMinion(minion);
        } else if (pdc.has(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY)) {
            event.setCancelled(true);

            byte[] data = pdc.get(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY);
            if (data == null)
                return;

            Minion minion = new Minion(event.getBlockPlaced().getLocation(), data);
            minionManager.registerMinion(minion);
        }

        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE)
            itemStack.setAmount(itemStack.getAmount() - 1);
    }

}
