package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiButtonFlag;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static dev.rosewood.roseminions.minion.module.InventoryModule.Settings.*;

public class InventoryModule extends MinionModule {

    public static class Settings implements PDCSettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<PDCRoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final PDCRoseSetting<Integer> INVENTORY_SIZE = define(PDCRoseSetting.ofInteger("inventory-size", 27, "How many individual items can be stored"));
        public static final PDCRoseSetting<ItemStack[]> INVENTORY_CONTENTS = define(PDCRoseSetting.ofHidden("inventory-contents", PDCSettingSerializers.ofArray(MinionSettingSerializers.ITEMSTACK), () -> new ItemStack[27]));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Inventory Module", Material.CHEST, MinionUtils.PRIMARY_COLOR + "Inventory Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to store items."))));
        }

        private Settings() { }

        @Override
        public List<PDCRoseSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> PDCRoseSetting<T> define(PDCRoseSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    public InventoryModule(Minion minion) {
        super(minion, DefaultMinionModules.INVENTORY, Settings.INSTANCE);
    }

    @Override
    public void finalizeLoad() {
        MinionUtils.snapInventorySize(this.settings, INVENTORY_SIZE, INVENTORY_CONTENTS);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        int rows = Math.max(1, Math.min((int) Math.ceil(this.settings.get(INVENTORY_SIZE) / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title())
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
                        .setClickAction(event -> event.isShiftClick() ? ClickAction.PAGE_FIRST : ClickAction.PAGE_BACKWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_FIRST_PAGE)
                        .setHiddenReplacement(borderItem))
                .addButtonAt(fullSize.getNumSlots() - 4, GuiFactory.createButton()
                        .setIcon(Material.PAPER)
                        .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Next Page (" + MinionUtils.SECONDARY_COLOR + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + MinionUtils.PRIMARY_COLOR + ")"))
                        .setClickAction(event -> event.isShiftClick() ? ClickAction.PAGE_LAST : ClickAction.PAGE_FORWARDS)
                        .setFlags(GuiButtonFlag.HIDE_IF_LAST_PAGE)
                        .setHiddenReplacement(borderItem));

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

    /**
     * Checks if this inventory contains an item matching the predicate
     *
     * @param predicate The predicate to run
     * @return true if the inventory contains the item
     */
    public boolean anyMatch(Predicate<ItemStack> predicate) {
        ItemStack[] contents = this.settings.get(INVENTORY_CONTENTS);
        for (ItemStack content : contents)
            if (content != null && predicate.test(content))
                return true;
        return false;
    }

    /**
     * Removes and returns the first item matching the predicate.
     * Takes only one item, not an entire stack.
     *
     * @param predicate The predicate to run
     * @return the single matching item removed from the inventory, or null if none matched
     */
    public ItemStack takeMatching(Predicate<ItemStack> predicate) {
        // Don't allow modifications while the GUI is open
        if (this.guiContainer != null && this.guiContainer.hasViewers())
            return null;

        ItemStack[] contents = this.settings.get(INVENTORY_CONTENTS);
        for (int i = 0; i < contents.length; i++) {
            ItemStack content = contents[i];
            if (content != null && predicate.test(content)) {
                int amount = content.getAmount();
                ItemStack clone = content.clone();
                clone.setAmount(1);
                amount -= 1;
                if (amount == 0) {
                    contents[i] = null;
                } else {
                    content.setAmount(amount);
                }
                return clone;
            }
        }

        return null;
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
