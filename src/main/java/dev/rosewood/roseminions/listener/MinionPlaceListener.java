package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
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
        if (event.getBlockPlaced().getType() != Material.PLAYER_HEAD)
            return;

        ItemStack itemStack = event.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        if (pdc.has(MinionUtils.MINION_NEW_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);

            String minionId = pdc.get(MinionUtils.MINION_NEW_KEY, PersistentDataType.STRING);
            if (minionId == null)
                return;

            MinionTypeManager minionTypeManager = this.rosePlugin.getManager(MinionTypeManager.class);
            MinionModuleManager minionModuleManager = this.rosePlugin.getManager(MinionModuleManager.class);

            MinionData minionData = minionTypeManager.getMinionData(minionId);
            if (minionData == null) {
                event.getPlayer().sendMessage("Invalid minion ID: " + minionId);
                return;
            }

            Minion minion = new Minion(minionId, 0, event.getPlayer().getUniqueId(), event.getBlockPlaced().getLocation(), false);

            MinionData.MinionRank rank = minionData.getRank(0);
            List<MinionModule> modules = rank.modules().entrySet().stream().map(entry -> {
                MinionModule module = minionModuleManager.createModule(entry.getKey(), minion);
                if (module == null)
                    throw new IllegalStateException("Failed to create module " + entry.getKey() + "!");
                module.mergeSettings(entry.getValue());
                return module;
            }).toList();
            minion.setModules(modules);

            minion.getAnimationController().mergeSettings(rank.animationSettings());

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
