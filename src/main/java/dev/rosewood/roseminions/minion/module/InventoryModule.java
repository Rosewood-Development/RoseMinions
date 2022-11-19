package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@MinionModuleInfo(name = "inventory")
public class InventoryModule extends MinionModule {

    private static final SettingAccessor<Integer> INVENTORY_SIZE;
    private static final SettingAccessor<ItemStack[]> INVENTORY_CONTENTS;

    static {
        INVENTORY_SIZE = SettingsContainer.defineSetting(InventoryModule.class, SettingSerializers.INTEGER, "inventory-size", 27, "How many individual items can be stored");
        INVENTORY_CONTENTS = SettingsContainer.defineHiddenSetting(InventoryModule.class, SettingSerializers.ITEMSTACK_ARRAY, "inventory-contents", new ItemStack[27]);
        SettingsContainer.redefineSetting(InventoryModule.class, MinionModule.GUI_ICON, Material.CHEST);
        SettingsContainer.redefineSetting(InventoryModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Inventory Module");
        SettingsContainer.redefineSetting(InventoryModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to store items.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings."));
    }

    public InventoryModule(Minion minion) {
        super(minion);

        // Adjust the inventory size if needed
        int size = this.settings.get(INVENTORY_SIZE);
        ItemStack[] contents = this.settings.get(INVENTORY_CONTENTS);
        if (contents.length != size) {
            ItemStack[] newContents = new ItemStack[size];
            System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, size));
            this.settings.set(INVENTORY_CONTENTS, newContents);
        }
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

        // Try to add to existing stacks or fill a new slot
        ItemStack[] contents = this.settings.get(INVENTORY_CONTENTS);
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                contents[i] = clone;
                return null;
            } else if (contents[i].isSimilar(clone)) {
                int remaining = contents[i].getAmount() + clone.getAmount();
                if (remaining <= maxStackSize) {
                    contents[i].setAmount(remaining);
                    return null;
                } else {
                    clone.setAmount(remaining - maxStackSize);
                    contents[i].setAmount(maxStackSize);
                }
            }
        }

        // Otherwise, return the remaining item
        return clone;
    }

}
