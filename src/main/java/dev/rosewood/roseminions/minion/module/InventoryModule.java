package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

@MinionModuleInfo(name = "inventory")
public class InventoryModule extends MinionModule {

    private static final SettingAccessor<Integer> INVENTORY_SIZE;

    static {
        INVENTORY_SIZE = SettingsContainer.defineSetting(InventoryModule.class, SettingSerializers.INTEGER, "inventory-size", 9, "How many individual items can be stored");
    }

    private final List<ItemStack> inventoryContents;

    public InventoryModule(Minion minion) {
        super(minion);
        this.inventoryContents = new ArrayList<>();
    }

    @Override
    public void update() {

    }

    /**
     * Attempts to add a copy of an item to the inventory
     *
     * @param item The item to add
     * @return null if the entire item was added, or a copy of the remaining item if it was not added completely
     */
    public ItemStack addItem(ItemStack item) {
        ItemStack clone = item.clone();
        int maxStackSize = clone.getMaxStackSize();

        // Try to add to existing stacks
        for (ItemStack existingStack : this.inventoryContents) {
            if (existingStack.isSimilar(clone)) {
                int remaining = existingStack.getAmount() + clone.getAmount();
                if (remaining <= maxStackSize) {
                    existingStack.setAmount(remaining);
                    return null;
                } else {
                    clone.setAmount(remaining - maxStackSize);
                    existingStack.setAmount(maxStackSize);
                }
            }
        }

        // Add to new stack if there is enough space
        if (this.inventoryContents.size() < this.settings.get(INVENTORY_SIZE)) {
            this.inventoryContents.add(clone);
            return null;
        }

        // Otherwise, return the remaining item
        return clone;
    }

}
