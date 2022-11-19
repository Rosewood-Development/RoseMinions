package dev.rosewood.roseminions.minion.gui;

import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import org.bukkit.entity.Player;

public abstract class GuiHolder {

    protected final Minion minion;
    protected final GuiFramework guiFramework;
    protected GuiContainer guiContainer;

    public GuiHolder(Minion minion) {
        this.minion = minion;
        this.guiFramework = GuiFramework.instantiate(RoseMinions.getInstance());
    }

    public void openFor(Player player) {
        if (this.isInvalid())
            this.buildGui();
        if (this.guiContainer != null)
            this.guiContainer.openFor(player);
    }

    protected abstract void buildGui();

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
