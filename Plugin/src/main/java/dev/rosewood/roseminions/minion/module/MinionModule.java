package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.KeyHelper;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.controller.ModuleController;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.minion.setting.SettingContainer;
import dev.rosewood.roseminions.object.GuiHolder;
import dev.rosewood.roseminions.object.Modular;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.object.PDCSerializable;
import dev.rosewood.roseminions.object.Updatable;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class MinionModule implements GuiHolder, PDCSerializable, Modular, Updatable {

    private static final NamespacedKey KEY_SETTINGS = KeyHelper.get("settings");
    private static final NamespacedKey KEY_MODULES = KeyHelper.get("modules");

    public static final PDCRoseSetting<ModuleGuiProperties> GUI_PROPERTIES = PDCRoseSetting.of("gui", ModuleGuiProperties.SERIALIZER, () ->
            new ModuleGuiProperties("GUI Title", Material.BARRIER, "Module",
                    List.of("", MinionUtils.SECONDARY_COLOR + "A minion module.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings.")),
            "Module GUI properties");

    protected final Minion minion;
    protected final String moduleName;
    protected final SettingContainer settings;
    protected final Map<Class<? extends MinionModule>, MinionModule> submodules;
    protected final List<ModuleController> activeControllers;
    protected Modular parentModular;

    protected final GuiFramework guiFramework;
    protected GuiContainer guiContainer;

    public MinionModule(Minion minion, String moduleName, PDCSettingHolder settings) {
        this.minion = minion;
        this.moduleName = moduleName.toLowerCase();
        this.settings = new SettingContainer(settings);
        this.submodules = new LinkedHashMap<>();
        this.activeControllers = new ArrayList<>();
        this.parentModular = minion;
        this.guiFramework = GuiFramework.instantiate(RoseMinions.getInstance());
    }

    /**
     * Called when the minion is completely finished loading and is ready for action
     */
    public void finalizeLoad() {

    }

    protected abstract void buildGui();

    public void unload() {
        this.kickOutViewers(); // Close all viewers

        // Unload functionality
        this.submodules.values().forEach(MinionModule::unload);

        // Unload controllers
        this.activeControllers.forEach(ModuleController::unload);

        // Unload GUI
        if (this.guiContainer != null)
            GuiFramework.getInstance().getGuiManager().unregisterGui(this.guiContainer);
    }

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

    public final SettingContainer getSettings() {
        return this.settings;
    }

    @Override
    public void writePDC(PersistentDataContainer container) {
        PersistentDataAdapterContext context = container.getAdapterContext();
        PersistentDataContainer settingsContainer = context.newPersistentDataContainer();
        this.settings.writePDC(settingsContainer);
        if (!settingsContainer.getKeys().isEmpty())
            container.set(KEY_SETTINGS, PersistentDataType.TAG_CONTAINER, settingsContainer);

        PersistentDataContainer modulesContainer = context.newPersistentDataContainer();
        for (MinionModule submodule : this.submodules.values()) {
            PersistentDataContainer moduleContainer = context.newPersistentDataContainer();
            submodule.writePDC(moduleContainer);
            if (!moduleContainer.getKeys().isEmpty())
                modulesContainer.set(KeyHelper.get(submodule.getName()), PersistentDataType.TAG_CONTAINER, moduleContainer);
        }
        if (!modulesContainer.getKeys().isEmpty())
            container.set(KEY_MODULES, PersistentDataType.TAG_CONTAINER, modulesContainer);
    }

    @Override
    public void readPDC(PersistentDataContainer container) {
        PersistentDataContainer settingsContainer = container.get(KEY_SETTINGS, PersistentDataType.TAG_CONTAINER);
        if (settingsContainer != null)
            this.settings.readPDC(settingsContainer);

        PersistentDataContainer modulesContainer = container.get(KEY_MODULES, PersistentDataType.TAG_CONTAINER);
        if (modulesContainer != null) {
            for (NamespacedKey key : modulesContainer.getKeys()) {
                String name = key.getKey();
                PersistentDataContainer moduleContainer = modulesContainer.get(key, PersistentDataType.TAG_CONTAINER);
                if (moduleContainer != null) {
                    // Find module with this name
                    Optional<MinionModule> module = this.submodules.values().stream()
                            .filter(x -> x.getName().equals(name))
                            .findFirst();

                    if (module.isPresent()) {
                        module.get().readPDC(moduleContainer); // TODO: Make sure values are still within allowed range
                    } else {
                        RoseMinions.getInstance().getLogger().warning("Skipped loading submodule " + name + " for minion at " + this.getMinion().getLocation() + " because the module no longer exists");
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass) {
        if (this.submodules.containsKey(moduleClass))
            return Optional.of((T) this.submodules.get(moduleClass));
        return this.parentModular.getModule(moduleClass);
    }

    public void tick() {

    }

    public void tickAsync() {

    }

    @Override
    public final void update() {
        this.submodules.values().forEach(MinionModule::update);
        this.activeControllers.forEach(ModuleController::update);
        this.tick();
    }

    @Override
    public final void updateAsync() {
        this.submodules.values().forEach(MinionModule::updateAsync);
        this.activeControllers.forEach(ModuleController::updateAsync);
        this.tickAsync();
    }

    public final Minion getMinion() {
        return this.minion;
    }

    public final String getName() {
        return this.moduleName;
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
