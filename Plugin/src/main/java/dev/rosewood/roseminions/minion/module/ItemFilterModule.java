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
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static dev.rosewood.roseminions.minion.module.ItemFilterModule.Settings.*;

public class ItemFilterModule extends MinionModule {

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<Integer> INVENTORY_SIZE = define(SettingAccessor.defineInteger("inventory-size", 27, "How many individual items can be stored for filtering"));
        public static final SettingAccessor<ItemStack[]> FILTER_ITEMS = define(SettingAccessor.defineHiddenSetting("filter-items", SettingSerializers.ofArray(SettingSerializers.ITEMSTACK), () -> new ItemStack[27]));
        public static final SettingAccessor<FilterType> FILTER_TYPE = define(SettingAccessor.defineEnum("filter-type", FilterType.BLACKLIST, "The type of filter"));
        public static final SettingAccessor<Boolean> MATCH_NBT = define(SettingAccessor.defineBoolean("match-nbt", false, "Whether or not to match NBT data"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Item Filter Module", Material.COMPARATOR, MinionUtils.PRIMARY_COLOR + "Item Filter Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to filter items."))));
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

    public ItemFilterModule(Minion minion) {
        super(minion, DefaultMinionModules.ITEM_FILTER, Settings.INSTANCE);

        MinionUtils.snapInventorySize(this.settings, INVENTORY_SIZE, FILTER_ITEMS);
    }

    @Override
    protected void buildGui() {
        MinionUtils.snapInventorySize(this.settings, INVENTORY_SIZE, FILTER_ITEMS);

        this.guiContainer = GuiFactory.createContainer();
        int rows = Math.max(1, Math.min((int) Math.ceil(this.settings.get(INVENTORY_SIZE) / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title())
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
