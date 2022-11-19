package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import org.bukkit.Material;

public abstract class MinionModule implements DataSerializable {

    public static final SettingAccessor<String> GUI_TITLE;
    public static final SettingAccessor<Material> GUI_ICON;
    public static final SettingAccessor<String> GUI_ICON_NAME;
    public static final SettingAccessor<List<String>> GUI_ICON_LORE;

    static {
        GUI_TITLE = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING, "gui-title", "Minion Module");
        GUI_ICON = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.MATERIAL, "gui-icon", Material.BARRIER, "The icon to use for this module in the minion GUI");
        GUI_ICON_NAME = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING, "gui-icon-name", MinionUtils.PRIMARY_COLOR + "Module", "The name to use for this module in the minion GUI");
        GUI_ICON_LORE = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING_LIST, "gui-icon-lore", List.of("", MinionUtils.SECONDARY_COLOR + "A minion module.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings."), "The lore to use for this module in the minion GUI");
    }

    protected final Minion minion;
    protected final SettingsContainer settings;

    public MinionModule(Minion minion) {
        this.minion = minion;
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

}
