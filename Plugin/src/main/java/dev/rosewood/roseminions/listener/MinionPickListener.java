package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.util.MinionUtils;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import java.util.Optional;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MinionPickListener implements Listener {

    private final RosePlugin rosePlugin;

    public MinionPickListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMinionPickEntity(PlayerPickEntityEvent event) {
        Entity entity = event.getEntity();
        Player player = event.getPlayer();
        if (!(entity instanceof ArmorStand armorStand) || player.getGameMode() != GameMode.CREATIVE)
            return;

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);
        Optional<Minion> minionOptional = minionManager.getMinionFromEntity(armorStand);
        if (minionOptional.isEmpty()) {
            if (minionManager.isMinion(armorStand))
                event.setCancelled(true); // TODO: This is a bad minion that isn't loaded but has data
            return;
        }

        if (event.getSourceSlot() != -1)
            return;

        event.setCancelled(true);

        Minion minion = minionOptional.get();
        ItemStack minionItem = minion.getRankData().getDisplayItemStack();
        ItemMeta itemMeta = minionItem.getItemMeta();

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        PersistentDataAdapterContext context = pdc.getAdapterContext();
        PersistentDataContainer dataContainer = context.newPersistentDataContainer();
        minion.writePDC(dataContainer);
        pdc.set(MinionUtils.MINION_DATA_KEY, PersistentDataType.TAG_CONTAINER, dataContainer);
        minionItem.setItemMeta(itemMeta);

        player.getInventory().setItem(event.getTargetSlot(), minionItem);
    }

}
