package dev.rosewood.roseminions.minion.gui;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.minion.module.MinionModule;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class MinionGui {

    private final Minion minion;
    private final GuiFramework guiFramework;
    private GuiContainer guiContainer;

    public MinionGui(Minion minion) {
        this.minion = minion;
        this.guiFramework = GuiFramework.instantiate(RoseMinions.getInstance());
    }

    public void openFor(Player player) {
        if (this.isInvalid())
            this.buildGui();
        this.guiContainer.openFor(player);
    }

    private void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(ChatColor.stripColor(HexUtils.colorify(this.minion.getRankData().getItemSettings().get(MinionData.MinionItem.DISPLAY_NAME))));

        for (MinionModule module : this.minion.getModules()) {
            mainScreen.addButton(GuiFactory.createButton()
                    .setIcon(module.getSettings().get(MinionModule.GUI_ICON))
                    .setName(HexUtils.colorify(module.getSettings().get(MinionModule.GUI_ICON_NAME)))
                    .setLore(module.getSettings().get(MinionModule.GUI_ICON_LORE).stream().map(HexUtils::colorify).toList()));
        }

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    /**
     * Forcefully closes the GUI for all viewers
     */
    public void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    /**
     * @return true if the GUI needs to be rebuilt, false otherwise
     */
    private boolean isInvalid() {
        return this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer);
    }

}
