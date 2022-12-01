package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingHolder;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.model.GuiHolder;
import dev.rosewood.roseminions.model.Modular;
import dev.rosewood.roseminions.model.Updatable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class MinionModule implements GuiHolder, SettingHolder, Modular, Updatable {

    public static final SettingAccessor<String> GUI_TITLE;
    public static final SettingAccessor<Material> GUI_ICON;
    public static final SettingAccessor<String> GUI_ICON_NAME;
    public static final SettingAccessor<List<String>> GUI_ICON_LORE;

    static {
        GUI_TITLE = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING, "gui-title", "GUI Title", "The title of the GUI");
        GUI_ICON = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.MATERIAL, "gui-icon", Material.BARRIER, "The icon to use for this module in the minion GUI");
        GUI_ICON_NAME = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.STRING, "gui-icon-name", "Module", "The name to use for this module in the minion GUI");
        GUI_ICON_LORE = SettingsContainer.defineSetting(MinionModule.class, SettingSerializers.ofList(SettingSerializers.STRING), "gui-icon-lore", List.of("", MinionUtils.SECONDARY_COLOR + "A minion module.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings."), "The lore to use for this module in the minion GUI");
    }

    protected final Minion minion;
    protected final SettingsContainer settings;
    protected final Map<Class<? extends MinionModule>, MinionModule> submodules;
    protected Modular parentModular;

    protected final GuiFramework guiFramework;
    protected GuiContainer guiContainer;

    public MinionModule(Minion minion) {
        this.minion = minion;
        this.settings = new SettingsContainer(this.getClass());
        this.submodules = new LinkedHashMap<>();
        this.parentModular = minion;
        this.guiFramework = GuiFramework.instantiate(RoseMinions.getInstance());
    }

    protected abstract void buildGui();

    @Override
    public final void openGui(Player player) {
        if (this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer))
            this.buildGui();

        if (this.guiContainer != null)
            this.guiContainer.openFor(player);
    }

    @Override
    public final void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    @Override
    public final SettingsContainer getSettings() {
        return this.settings;
    }

    @Override
    public byte[] serialize() {
        return DataSerializable.write(outputStream -> {
            byte[] settingsData = SettingHolder.super.serialize();
            outputStream.writeInt(settingsData.length);
            outputStream.write(settingsData);

            outputStream.writeInt(this.submodules.size());
            for (MinionModule submodule : this.submodules.values()) {
                outputStream.writeUTF(submodule.getName());
                byte[] submoduleData = submodule.serialize();
                outputStream.writeInt(submoduleData.length);
                outputStream.write(submoduleData);
            }
        });
    }

    @Override
    public void deserialize(byte[] input) {
        DataSerializable.read(input, inputStream -> {
            byte[] settingsData = new byte[inputStream.readInt()];
            inputStream.read(settingsData);
            SettingHolder.super.deserialize(settingsData);

            int submodulesSize = inputStream.readInt();
            for (int i = 0; i < submodulesSize; i++) {
                String name = inputStream.readUTF();
                byte[] submoduleData = new byte[inputStream.readInt()];
                inputStream.read(submoduleData);

                // Find module with this name
                Optional<MinionModule> module = this.submodules.values().stream()
                        .filter(x -> x.getName().equals(name))
                        .findFirst();

                if (module.isPresent()) {
                    module.get().deserialize(submoduleData); // TODO: Make sure values are still within allowed range
                } else {
                    RoseMinions.getInstance().getLogger().warning("Skipped loading submodule " + name + " for minion at " + this.getMinion().getLocation() + " because the module no longer exists");
                }
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass) {
        if (this.submodules.containsKey(moduleClass))
            return Optional.of((T) this.submodules.get(moduleClass));
        return this.parentModular.getModule(moduleClass);
    }

    @Override
    public void update() {
        this.submodules.values().forEach(MinionModule::update);
    }

    @Override
    public void updateAsync() {
        this.submodules.values().forEach(MinionModule::updateAsync);
    }

    public final Minion getMinion() {
        return this.minion;
    }

    public final String getName() {
        MinionModuleInfo info = this.getClass().getAnnotation(MinionModuleInfo.class);
        if (info == null)
            throw new IllegalStateException("MinionModuleInfo annotation not found on " + this.getClass().getName());
        return info.name().toLowerCase();
    }

    public final void setSubModules(Map<Class<? extends MinionModule>, MinionModule> modules) {
        this.submodules.clear();
        this.submodules.putAll(modules);
        this.submodules.values().forEach(x -> x.parentModular = this);
    }

    protected final void addBackButton(GuiScreen guiScreen) {
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
