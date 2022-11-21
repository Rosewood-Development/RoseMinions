package dev.rosewood.roseminions.minion.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.ClickActionType;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MinionGui extends GuiHolder {

    private static final List<Integer> MODULE_SLOT_FILL_ORDER;

    static {
        IntStream fillOrder = IntStream.of(16, 15, 14, 13, 12);
        fillOrder = IntStream.concat(fillOrder, IntStream.rangeClosed(21, 25));
        fillOrder = IntStream.concat(fillOrder, IntStream.rangeClosed(30, 34));
        fillOrder = IntStream.concat(fillOrder, IntStream.rangeClosed(39, 43));
        MODULE_SLOT_FILL_ORDER = fillOrder.boxed().toList();
    }

    public MinionGui(Minion minion) {
        super(minion);
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        List<MinionModule> modules = new ArrayList<>(this.minion.getModules());
        if (modules.size() > 20)
            throw new IllegalStateException("Cannot have more than 20 modules");

        // Sort alphabetically, reverse the order of the first 5 modules
        modules.sort(Comparator.comparing(MinionModule::getName));
        modules.subList(0, Math.min(5, modules.size())).sort(Comparator.comparing(MinionModule::getName).reversed());

        // Find the GUI size based on how many modules there are
        int rows = 1 + (int) Math.ceil(MODULE_SLOT_FILL_ORDER.get(modules.size() - 1) / 9.0);
        GuiSize size = GuiSize.fromRows(rows);

        ItemStack displayItem = this.minion.getRankData().getItemStack(false);
        ItemMeta displayItemMeta = displayItem.getItemMeta();
        if (displayItemMeta != null) {
            displayItemMeta.setLore(null);
            displayItem.setItemMeta(displayItemMeta);
        }

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, size)
                .setTitle(ChatColor.stripColor(HexUtils.colorify(this.minion.getRankData().getItemSettings().get(MinionData.MinionItem.DISPLAY_NAME))))
                .addButtonAt(10, GuiFactory.createButton(displayItem)
                        .setName(HexUtils.colorify(this.minion.getRankData().getItemSettings().get(MinionData.MinionItem.DISPLAY_NAME)))
                        .setLore(List.of(
                                "",
                                HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Left-click to view rank information."),
                                HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Right-click to edit appearance settings.")
                        )));

        int moduleIndex = 0;
        for (MinionModule module : modules) {
            mainScreen.addButtonAt(MODULE_SLOT_FILL_ORDER.get(moduleIndex++), GuiFactory.createButton()
                    .setIcon(module.getSettings().get(MinionModule.GUI_ICON))
                    .setName(HexUtils.colorify(module.getSettings().get(MinionModule.GUI_ICON_NAME)))
                    .setLore(module.getSettings().get(MinionModule.GUI_ICON_LORE).stream().map(HexUtils::colorify).toList())
                    .setClickAction(event -> {
                        module.openFor((Player) event.getWhoClicked());
                        return ClickAction.NOTHING;
                    }, ClickActionType.LEFT_CLICK, ClickActionType.SHIFT_RIGHT_CLICK));
        }

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

}
