package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UpgradeModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Upgrade Minion", Material.IRON_SWORD, MinionUtils.PRIMARY_COLOR + "Upgrade Minion",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows upgrading this minion."))));
        }

        private Settings() { }

        @Override
        public List<RoseSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> RoseSetting<T> define(RoseSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    public UpgradeModule(Minion minion) {
        super(minion, DefaultMinionModules.UPGRADE, Settings.INSTANCE);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();
        GuiSize fullSize = GuiSize.ROWS_THREE;

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

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
