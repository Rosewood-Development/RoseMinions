package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.util.MinionUtils;
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
        Minion minion = minionManager.getMinionFromEntity(armorStand);
        if (minion == null)
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!minion.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("You do not own this minion");
            return;
        }

        if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            MinionData minionData = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(minion.getTypeId());
            if (minionData == null) {
                event.getPlayer().sendMessage("Invalid minion ID: " + minion.getTypeId());
                return;
            }

            byte[] data = minion.serialize();
            minionManager.destroyMinion(minion);

            ItemStack itemStack = minionData.getItemStack(minion.getRank(), false);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null)
                throw new IllegalStateException("ItemStack does not have any ItemMeta");

            PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
            pdc.set(MinionUtils.MINION_DATA_KEY, PersistentDataType.BYTE_ARRAY, data);
            itemStack.setItemMeta(itemMeta);

            PlayerInventory inventory = player.getInventory();
            inventory.setItem(inventory.getHeldItemSlot(), itemStack);
        } else {

        }
    }

}
