package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiButtonFlag;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

@MinionModuleInfo(name = "filter")
public class FilterModule extends MinionModule {

    public static final SettingAccessor<Integer> INVENTORY_SIZE;
    public static final SettingAccessor<ItemStack[]> FILTER_ITEMS;
    public static final SettingAccessor<FilterType> FILTER_TYPE;
    public static final SettingAccessor<Boolean> MATCH_NBT;

    static {
        INVENTORY_SIZE = SettingsContainer.defineSetting(FilterModule.class, SettingSerializers.INTEGER, "inventory-size", 27, "How many individual items can be stored");
        FILTER_ITEMS = SettingsContainer.defineHiddenSetting(FilterModule.class, SettingSerializers.ofArray(SettingSerializers.ITEMSTACK), "filter-items", new ItemStack[27]);
        FILTER_TYPE = SettingsContainer.defineSetting(FilterModule.class, SettingSerializers.ofEnum(FilterType.class), "filter-type", FilterType.BLACKLIST, "The type of filter");
        MATCH_NBT = SettingsContainer.defineSetting(FilterModule.class, SettingSerializers.BOOLEAN, "match-nbt", false, "Whether or not to match NBT data");

        SettingsContainer.redefineSetting(FilterModule.class, MinionModule.GUI_TITLE, "Filter Module");
        SettingsContainer.redefineSetting(FilterModule.class, MinionModule.GUI_ICON, Material.COMPARATOR);
        SettingsContainer.redefineSetting(FilterModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Filter Module");
        SettingsContainer.redefineSetting(FilterModule.class, MinionModule.GUI_ICON_LORE, List.of(
                "",
                MinionUtils.SECONDARY_COLOR + "Allows the minion to filter items.",
                MinionUtils.SECONDARY_COLOR + "Left-click to open.",
                MinionUtils.SECONDARY_COLOR + "Right-click to edit settings.")
        );

    }

    public FilterModule(Minion minion) {
        super(minion);
    }

    @Override
    public void update() {

    }

    @Override
    protected void buildGui() {
        this.fixInventorySize();

        this.guiContainer = GuiFactory.createContainer();


        int rows = Math.max(1, Math.min((int) Math.ceil(this.settings.get(INVENTORY_SIZE) / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE))
                .setEditableSection(0, editableSize.getNumSlots() - 1, Arrays.asList(this.settings.get(FILTER_ITEMS)), (player, items) -> {
                    ItemStack[] contents = new ItemStack[this.settings.get(INVENTORY_SIZE)];
                    System.arraycopy(items.toArray(ItemStack[]::new), 0, contents, 0, Math.min(items.size(), contents.length));
                    this.settings.set(FILTER_ITEMS, contents);
                });

        // Fill inventory border with glass for now
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

        // Add switch filter type button
        mainScreen.addButtonAt(fullSize.getNumSlots() - 5, GuiFactory.createButton()
                .setIcon(Material.HOPPER)
                .setNameSupplier(() -> GuiFactory.createString(
                        HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Filter Type (" + MinionUtils.SECONDARY_COLOR + this.settings.get(FILTER_TYPE).name() + MinionUtils.PRIMARY_COLOR + ")"))
                )
                .setClickAction(event -> {
                    FilterType filterType = this.settings.get(FILTER_TYPE);
                    filterType = filterType == FilterType.BLACKLIST ? FilterType.WHITELIST : FilterType.BLACKLIST;
                    this.settings.set(FILTER_TYPE, filterType);
                    return ClickAction.REFRESH;
                }));

        // Add match NBT button
        mainScreen.addButtonAt(fullSize.getNumSlots() - 3, GuiFactory.createButton()
                .setIcon(Material.NETHER_STAR)
                .setNameSupplier(() -> GuiFactory.createString(
                        HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Match NBT (" + MinionUtils.SECONDARY_COLOR + this.settings.get(MATCH_NBT) + MinionUtils.PRIMARY_COLOR + ")"))
                )
                .setClickAction(event -> {
                    boolean matchNbt = this.settings.get(MATCH_NBT);
                    this.settings.set(MATCH_NBT, !matchNbt);
                    return ClickAction.REFRESH;
                }));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);

    }

    private void fixInventorySize() {
        // Make sure the inventory size is always either 9, 18, 27, or intervals of 27
        int inventorySize = this.settings.get(INVENTORY_SIZE);
        if (inventorySize % 9 != 0) {
            if (inventorySize < 9) {
                inventorySize = 9;
            } else if (inventorySize < 18) {
                inventorySize = 18;
            } else if (inventorySize < 27) {
                inventorySize = 27;
            }
        }

        if (inventorySize > 27)
            inventorySize = (int) Math.ceil(inventorySize / 27.0) * 27;

        if (inventorySize != this.settings.get(INVENTORY_SIZE))
            this.settings.set(INVENTORY_SIZE, inventorySize);

        // Adjust the inventory size if needed
        int size = this.settings.get(INVENTORY_SIZE);
        ItemStack[] contents = this.settings.get(FILTER_ITEMS);
        if (contents.length != size) {
            ItemStack[] newContents = new ItemStack[size];
            System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, size));
            this.settings.set(FILTER_ITEMS, newContents);
        }
    }

    /**
     * Check if the given item is allowed by this filter
     *
     * @param itemStack The item to check
     * @return true if the item is allowed, false otherwise
     */
    public boolean isFiltered(ItemStack itemStack) {
        ItemStack[] filterItems = this.settings.get(FILTER_ITEMS);
        FilterType filterType = this.settings.get(FILTER_TYPE);
        boolean matchNbt = this.settings.get(MATCH_NBT);

        // check if the item is in the filter
        boolean inFilter = false;
        for (ItemStack filterItem : filterItems) {
            if (filterItem == null || filterItem.getType() == Material.AIR)
                continue;

            if (matchNbt && filterItem.isSimilar(itemStack) || !matchNbt && filterItem.getType() == itemStack.getType()) {
                inFilter = true;
                break;
            }
        }

        return (filterType == FilterType.BLACKLIST) == inFilter;
    }

    public enum FilterType {
        WHITELIST,
        BLACKLIST
    }

}
