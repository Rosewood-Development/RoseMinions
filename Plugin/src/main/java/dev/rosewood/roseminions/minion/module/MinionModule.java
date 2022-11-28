package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.gui.GuiHolder;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class MinionModule extends GuiHolder implements DataSerializable {

    public static final SettingAccessor<String> GUI_TITLE;
    public static final SettingAccessor<Material> GUI_ICON;
    public static final SettingAccessor<String> GUI_ICON_NAME;
    public static final SettingAccessor<List<String>> GUI_ICON_LORE;

    static {
        GUI_TITLE = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING, "gui-title", "Minion Module", "The title of the GUI for this module");
        GUI_ICON = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.MATERIAL, "gui-icon", Material.BARRIER, "The icon to use for this module in the minion GUI");
        GUI_ICON_NAME = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING, "gui-icon-name", "Module", "The name to use for this module in the minion GUI");
        GUI_ICON_LORE = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.ofList(SettingSerializers.STRING), "gui-icon-lore", List.of("", MinionUtils.SECONDARY_COLOR + "A minion module.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings."), "The lore to use for this module in the minion GUI");
    }

    protected final SettingsContainer settings;

    public MinionModule(Minion minion) {
        super(minion);
        this.settings = new SettingsContainer(this.getClass());
    }

    public void mergeSettings(SettingsContainer settings) {
        this.settings.merge(settings);
    }

    public SettingsContainer getSettings() {
        return this.settings;
    }

    @Override
    public final byte[] serialize() {
        return this.settings.serialize();
    }

    @Override
    public final void deserialize(byte[] input) {
        this.settings.deserialize(input);
    }

    public String getName() {
        MinionModuleInfo info = this.getClass().getAnnotation(MinionModuleInfo.class);
        if (info == null)
            throw new IllegalStateException("MinionModuleInfo annotation not found on " + this.getClass().getName());
        return info.name().toLowerCase();
    }

    public abstract void update();

    public void updateAsync() {
        // Does nothing by default
    }

    protected void addBackButton(GuiScreen guiScreen) {
        guiScreen.addButtonAt(guiScreen.getCurrentSize().getNumSlots() - 1, GuiFactory.createButton()
                .setIcon(Material.ARROW)
                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Back"))
                .setLore(List.of("", HexUtils.colorify(MinionUtils.SECONDARY_COLOR + "Click to go back")))
                .setClickAction(event -> {
                    this.minion.openGui((Player) event.getWhoClicked());
                    return ClickAction.NOTHING;
                }));
    }

}
