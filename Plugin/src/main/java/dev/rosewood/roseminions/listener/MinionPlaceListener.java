package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
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
        Block blockPlaced = event.getBlockPlaced();
        if (blockPlaced.getType() != Material.PLAYER_HEAD && blockPlaced.getType() != Material.PLAYER_WALL_HEAD)
            return;

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);

        Location placeLocation;
        Block placedAgainst = event.getBlockAgainst();
        if (placedAgainst.equals(blockPlaced.getRelative(BlockFace.DOWN)) && !placedAgainst.getType().isCollidable()) {
            placeLocation = placedAgainst.getLocation();
        } else {
            placeLocation = blockPlaced.getLocation();
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        if (pdc.has(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);

            String minionId = pdc.get(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING);
            if (minionId == null)
                return;

            String minionRank = pdc.get(MinionUtils.MINION_NEW_RANK_KEY, PersistentDataType.STRING);
            if (minionRank == null)
                return;

            MinionConfig minionConfig = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(minionId);
            if (minionConfig == null) {
                player.sendMessage("Invalid minion ID: " + minionId);
                return;
            }

            Minion minion = new Minion(minionConfig, minionRank, player.getUniqueId(), placeLocation);
            minionManager.registerMinion(minion);
        } else if (pdc.has(MinionUtils.MINION_DATA_KEY, PersistentDataType.TAG_CONTAINER)) {
            event.setCancelled(true);

            PersistentDataContainer dataContainer = pdc.get(MinionUtils.MINION_DATA_KEY, PersistentDataType.TAG_CONTAINER);
            if (dataContainer == null)
                return;

            Minion minion = new Minion(placeLocation, dataContainer, player.getUniqueId());
            minionManager.registerMinion(minion);
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            this.rosePlugin.getScheduler().runTask(() -> itemStack.setAmount(itemStack.getAmount() - 1));
        } else {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

}
