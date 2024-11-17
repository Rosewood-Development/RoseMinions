package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UpgradeModule extends MinionModule {

    static {
        SettingsRegistry.redefineString(AppearanceModule.class, MinionModule.GUI_TITLE, "Upgrade Minion");
        SettingsRegistry.redefineEnum(AppearanceModule.class, MinionModule.GUI_ICON, Material.NETHER_STAR);
        SettingsRegistry.redefineString(AppearanceModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Minion Appearance");
        SettingsRegistry.redefineStringList(AppearanceModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows modifying the minion's appearance.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    public UpgradeModule(Minion minion) {
        super(minion, DefaultMinionModules.UPGRADE);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();
        GuiSize fullSize = GuiSize.ROWS_THREE;

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        // Fill inventory with glass for now
        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = borderItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(" ");
            itemMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(itemMeta);
        }

        GuiUtil.fillScreen(mainScreen, borderItem);

//        // Add switch filter type button
//        mainScreen.addButtonAt(fullSize.getNumSlots() - 5, GuiFactory.createButton()
//                .setIcon(Material.HOPPER)
//                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Filter Type (" + MinionUtils.SECONDARY_COLOR + this.settings.get(FILTER_TYPE).name() + MinionUtils.PRIMARY_COLOR + ")")))
//                .setClickAction(event -> {
//                    FilterModule.FilterType filterType = this.settings.get(FILTER_TYPE);
//                    filterType = filterType == FilterModule.FilterType.BLACKLIST ? FilterModule.FilterType.WHITELIST : FilterModule.FilterType.BLACKLIST;
//                    this.settings.set(FILTER_TYPE, filterType);
//                    return ClickAction.REFRESH;
//                }));
//
//        // Add match NBT button
//        mainScreen.addButtonAt(fullSize.getNumSlots() - 3, GuiFactory.createButton()
//                .setIcon(Material.NETHER_STAR)
//                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Match NBT (" + MinionUtils.SECONDARY_COLOR + this.settings.get(MATCH_NBT) + MinionUtils.PRIMARY_COLOR + ")")))
//                .setClickAction(event -> {
//                    return ClickAction.CLOSE;
//                }));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

}
