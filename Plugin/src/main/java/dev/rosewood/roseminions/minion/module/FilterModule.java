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
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FilterModule extends MinionModule {

    public static final SettingAccessor<Integer> INVENTORY_SIZE;
    public static final SettingAccessor<ItemStack[]> FILTER_ITEMS;
    public static final SettingAccessor<FilterType> FILTER_TYPE;
    public static final SettingAccessor<Boolean> MATCH_NBT;

    static {
        INVENTORY_SIZE = SettingsRegistry.defineInteger(FilterModule.class, "inventory-size", 27, "How many individual items can be stored for filtering");
        FILTER_ITEMS = SettingsRegistry.defineHiddenSetting(FilterModule.class, SettingSerializers.ofArray(SettingSerializers.ITEMSTACK), "filter-items", () -> new ItemStack[27]);
        FILTER_TYPE = SettingsRegistry.defineEnum(FilterModule.class, "filter-type", FilterType.BLACKLIST, "The type of filter");
        MATCH_NBT = SettingsRegistry.defineBoolean(FilterModule.class, "match-nbt", false, "Whether or not to match NBT data");

        SettingsRegistry.redefineString(FilterModule.class, MinionModule.GUI_TITLE, "Filter Module");
        SettingsRegistry.redefineEnum(FilterModule.class, MinionModule.GUI_ICON, Material.COMPARATOR);
        SettingsRegistry.redefineString(FilterModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Filter Module");
        SettingsRegistry.redefineStringList(FilterModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to filter items.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    public FilterModule(Minion minion) {
        super(minion, DefaultMinionModules.FILTER);
    }

    @Override
    public void update() {

    }

    @Override
    protected void buildGui() {
        MinionUtils.snapInventorySize(this.settings, INVENTORY_SIZE, FILTER_ITEMS);

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

        mainScreen.addButtonAt(fullSize.getNumSlots() - 6, GuiFactory.createButton()
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
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Filter Type (" + MinionUtils.SECONDARY_COLOR + this.settings.get(FILTER_TYPE).name() + MinionUtils.PRIMARY_COLOR + ")")))
                .setClickAction(event -> {
                    FilterType filterType = this.settings.get(FILTER_TYPE);
                    filterType = filterType == FilterType.BLACKLIST ? FilterType.WHITELIST : FilterType.BLACKLIST;
                    this.settings.set(FILTER_TYPE, filterType);
                    return ClickAction.REFRESH;
                }));

        // Add match NBT button
        mainScreen.addButtonAt(fullSize.getNumSlots() - 3, GuiFactory.createButton()
                .setIcon(Material.NETHER_STAR)
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Match NBT (" + MinionUtils.SECONDARY_COLOR + this.settings.get(MATCH_NBT) + MinionUtils.PRIMARY_COLOR + ")")))
                .setClickAction(event -> {
                    boolean matchNbt = this.settings.get(MATCH_NBT);
                    this.settings.set(MATCH_NBT, !matchNbt);
                    return ClickAction.REFRESH;
                }));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    /**
     * Check if the given item is allowed by this filter
     *
     * @param itemStack The item to check
     * @return true if the item is allowed, false otherwise
     */
    public boolean isAllowed(ItemStack itemStack) {
        ItemStack[] filterItems = this.settings.get(FILTER_ITEMS);
        FilterType filterType = this.settings.get(FILTER_TYPE);
        boolean matchNbt = this.settings.get(MATCH_NBT);

        // Check if the item is in the filter
        boolean inFilter = false;
        for (ItemStack filterItem : filterItems) {
            if (filterItem == null || filterItem.getType() == Material.AIR)
                continue;

            if ((matchNbt && filterItem.isSimilar(itemStack)) || (!matchNbt && filterItem.getType() == itemStack.getType())) {
                inFilter = true;
                break;
            }
        }

        // If the item is in the filter, return true if the filter type is whitelist, false otherwise
        // If the item is not in the filter, return true if the filter type is blacklist, false otherwise
        if (inFilter) {
            return filterType == FilterType.WHITELIST;
        } else {
            return filterType == FilterType.BLACKLIST;
        }
    }

    public enum FilterType {
        WHITELIST,
        BLACKLIST
    }

}
