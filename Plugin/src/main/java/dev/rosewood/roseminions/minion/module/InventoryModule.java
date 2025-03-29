package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiButtonFlag;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static dev.rosewood.roseminions.minion.module.InventoryModule.Settings.*;

public class InventoryModule extends MinionModule {

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<Integer> INVENTORY_SIZE = define(SettingAccessor.defineInteger("inventory-size", 27, "How many individual items can be stored"));
        public static final SettingAccessor<ItemStack[]> INVENTORY_CONTENTS = define(SettingAccessor.defineHiddenSetting("inventory-contents", SettingSerializers.ofArray(SettingSerializers.ITEMSTACK), () -> new ItemStack[27]));

        static {
            define(MinionModule.GUI_TITLE.copy("Inventory Module"));
            define(MinionModule.GUI_ICON.copy(Material.CHEST));
            define(MinionModule.GUI_ICON_NAME.copy(MinionUtils.PRIMARY_COLOR + "Inventory Module"));
            define(MinionModule.GUI_ICON_LORE.copy(List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to store items.")));
        }

        private Settings() { }

        @Override
        public List<SettingAccessor<?>> get() {
            return Collections.unmodifiableList(ACCESSORS);
        }

        private static <T> SettingAccessor<T> define(SettingAccessor<T> accessor) {
            ACCESSORS.add(accessor);
            return accessor;
        }

    }

    public InventoryModule(Minion minion) {
        super(minion, DefaultMinionModules.INVENTORY, Settings.INSTANCE);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        int rows = Math.max(1, Math.min((int) Math.ceil(this.settings.get(INVENTORY_SIZE) / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE))
                .setEditableSection(0, editableSize.getNumSlots() - 1, Arrays.asList(this.settings.get(INVENTORY_CONTENTS)), (player, items) -> {
                    ItemStack[] contents = new ItemStack[this.settings.get(INVENTORY_SIZE)];
                    System.arraycopy(items.toArray(ItemStack[]::new), 0, contents, 0, Math.min(items.size(), contents.length));
                    this.settings.set(INVENTORY_CONTENTS, contents);
                });

        // Fill inventory border with glass for util buttons
        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }
        GuiUtil.fillRow(mainScreen, editableSize.getRows(), borderItem);

        mainScreen
                .addButtonAt(fullSize.getNumSlots() - 6, GuiFactory.createButton()
                        .setIcon(Material.PAPER)
                        .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Previous Page (" + MinionUtils.SECONDARY_COLOR + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + MinionUtils.PRIMARY_COLOR + ")"))
                        .setItemFlags()
                        .setClickAction(event -> event.isShiftClick() ? ClickAction.PAGE_FIRST : ClickAction.PAGE_BACKWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_FIRST_PAGE)
                        .setHiddenReplacement(borderItem))
                .addButtonAt(fullSize.getNumSlots() - 4, GuiFactory.createButton()
                        .setIcon(Material.PAPER)
                        .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Next Page (" + MinionUtils.SECONDARY_COLOR + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + MinionUtils.PRIMARY_COLOR + ")"))
                        .setItemFlags()
                        .setClickAction(event -> event.isShiftClick() ? ClickAction.PAGE_LAST : ClickAction.PAGE_FORWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_LAST_PAGE)
                        .setHiddenReplacement(borderItem));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    @Override
    public void finalizeLoad() {
        MinionUtils.snapInventorySize(this.settings, INVENTORY_SIZE, INVENTORY_CONTENTS);
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

    /**
     * Attempts to remove a copy of an item from the inventory
     *
     * @param item The item to remove
     * @return true if the item was removed, false if it was not
     */
    public boolean removeItem(ItemStack item) {
        ItemStack clone = item.clone();

        // Don't allow modifications while the GUI is open
        if (this.guiContainer != null && this.guiContainer.hasViewers())
            return false;

        ItemStack[] contents = this.settings.get(INVENTORY_CONTENTS);
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].isSimilar(clone)) {
                int remaining = contents[i].getAmount() - clone.getAmount();
                if (remaining <= 0) {
                    contents[i] = null;
                } else {
                    contents[i].setAmount(remaining);
                }
                return true;
            }
        }

        return false;
    }

}
