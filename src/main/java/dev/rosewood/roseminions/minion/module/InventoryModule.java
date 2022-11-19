package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@MinionModuleInfo(name = "inventory")
public class InventoryModule extends MinionModule {

    private static final SettingAccessor<Integer> INVENTORY_SIZE;
    private static final SettingAccessor<ItemStack[]> INVENTORY_CONTENTS;

    static {
        INVENTORY_SIZE = SettingsContainer.defineSetting(InventoryModule.class, SettingSerializers.INTEGER, "inventory-size", 27, "How many individual items can be stored");
        INVENTORY_CONTENTS = SettingsContainer.defineHiddenSetting(InventoryModule.class, SettingSerializers.ITEMSTACK_ARRAY, "inventory-contents", new ItemStack[27]);
        SettingsContainer.redefineSetting(InventoryModule.class, MinionModule.GUI_TITLE, "Inventory Module");
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

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_FIVE)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE))
                .setEditableSection(0, Math.min(this.settings.get(INVENTORY_SIZE) - 1, GuiSize.ROWS_THREE.getNumSlots() - 1), Arrays.asList(this.settings.get(INVENTORY_CONTENTS)), (player, items) -> {
                    ItemStack[] contents = new ItemStack[this.settings.get(INVENTORY_SIZE)];
                    System.arraycopy(items.toArray(ItemStack[]::new), 0, contents, 0, Math.min(items.size(), contents.length));
                    this.settings.set(INVENTORY_CONTENTS, contents);
                });

        // Fill inventory border with class for util buttons
        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }
        GuiUtil.fillRow(mainScreen, 3, borderItem);

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    /**
     * Attempts to add a copy of an item to the inventory
     *
     * @param item The item to add
     * @return null if the entire item was added, or a copy of the remaining item if it was not added completely
     */
    public ItemStack addItem(ItemStack item) {
        ItemStack clone = item.clone();

        // Don't allow modifications while the GUI is open
        if (this.guiContainer != null && this.guiContainer.hasViewers())
            return clone;

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
