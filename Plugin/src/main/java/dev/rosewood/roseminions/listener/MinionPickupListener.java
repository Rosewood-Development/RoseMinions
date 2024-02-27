package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MinionPickupListener implements Listener {

    private final RosePlugin rosePlugin;

    public MinionPickupListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMinionBreak(PlayerArmorStandManipulateEvent event) {
        ArmorStand armorStand = event.getRightClicked();
        if (event.getSlot() != EquipmentSlot.HEAD)
            return;

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);
        Optional<Minion> minionOptional = minionManager.getMinionFromEntity(armorStand);
        if (minionOptional.isEmpty()) {
            if (minionManager.isMinion(armorStand))
                event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        Minion minion = minionOptional.get();
        Player player = event.getPlayer();
        if (!minion.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("You do not own this minion");
            return;
        }

        if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            byte[] data = minion.serialize();
            minionManager.destroyMinion(minion);

            ItemStack itemStack = minion.getRankData().getDisplayItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null)
                throw new IllegalStateException("ItemStack does not have any ItemMeta");

            PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
            pdc.set(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY, data);
            itemStack.setItemMeta(itemMeta);

            PlayerInventory inventory = player.getInventory();
            inventory.setItem(inventory.getHeldItemSlot(), itemStack);
        } else {
            minion.openGui(player);
        }
    }

}
