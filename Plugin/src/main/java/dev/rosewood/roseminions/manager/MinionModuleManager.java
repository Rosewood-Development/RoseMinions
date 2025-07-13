package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.MinionModuleRegistrationEvent;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.module.AttackingModule;
import dev.rosewood.roseminions.minion.module.BeeKeepingModule;
import dev.rosewood.roseminions.minion.module.BlockBreakModule;
import dev.rosewood.roseminions.minion.module.BlockFilterModule;
import dev.rosewood.roseminions.minion.module.BlockPlaceModule;
import dev.rosewood.roseminions.minion.module.CommunicationModule;
import dev.rosewood.roseminions.minion.module.DefaultMinionModules;
import dev.rosewood.roseminions.minion.module.ExperiencePickupModule;
import dev.rosewood.roseminions.minion.module.FarmingModule;
import dev.rosewood.roseminions.minion.module.FishingModule;
import dev.rosewood.roseminions.minion.module.InventoryModule;
import dev.rosewood.roseminions.minion.module.ItemFilterModule;
import dev.rosewood.roseminions.minion.module.ItemPickupModule;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.module.PotionEffectModule;
import dev.rosewood.roseminions.minion.module.ShearingModule;
import dev.rosewood.roseminions.minion.module.UpgradeModule;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.minion.setting.SettingContainerConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MinionModuleManager extends Manager implements Listener {

    private final Map<String, RegisteredMinionModule<?>> registeredModules;
    private final Map<String, SettingContainerConfig> moduleSettings;

    public MinionModuleManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.registeredModules = new HashMap<>();
        this.moduleSettings = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, this.rosePlugin);
    }

    @Override
    public void reload() {
        MinionModuleRegistrationEvent event = new MinionModuleRegistrationEvent();
        Bukkit.getPluginManager().callEvent(event);

        for (Map.Entry<String, RegisteredMinionModule<?>> entry : event.getRegisteredModules().entrySet())
            this.createAndLoadModuleFile(entry.getKey(), entry.getValue());
    }

    @Override
    public void disable() {
        this.registeredModules.clear();
        this.moduleSettings.clear();
    }

    public MinionModule createModule(String name, Minion minion) {
        RegisteredMinionModule<?> registeredModule = this.registeredModules.get(name);
        if (registeredModule == null)
            return null;

        try {
            MinionModule module = registeredModule.factory().apply(minion);
            if (!module.getName().equals(name))
                throw new IllegalStateException("Module name does not match the name of the module being created. Expected " + name + " but got " + module.getName());
            return module;
        } catch (Exception e) {
            this.rosePlugin.getLogger().warning("Failed to create module " + name.toLowerCase() + "!");
            e.printStackTrace();
        }

        return null;
    }

    public SettingContainerConfig getModuleSettingConfig(String name) {
        return this.moduleSettings.get(name).copy();
    }

    public boolean isValidModule(String name) {
        return this.registeredModules.containsKey(name.toLowerCase());
    }

    public SettingContainerConfig getSectionSettings(String name, ConfigurationSection section) {
        RegisteredMinionModule<?> registeredModule = this.registeredModules.get(name);
        if (registeredModule != null)
            return new SettingContainerConfig(registeredModule.settings(), section);
        return null;
    }

    private void createAndLoadModuleFile(String name, RegisteredMinionModule<?> registeredModule) {
        File directory = new File(this.rosePlugin.getDataFolder(), "modules");
        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, name + ".yml");
        boolean changed = !file.exists();
        CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);

        for (RoseSetting<?> setting : registeredModule.settings().get()) {
            if (!setting.readIsValid(config)) {
                setting.writeWithDefault(config);
                changed = true;
            }
        }

        if (changed)
            config.save(file);

        this.registeredModules.put(name, registeredModule);

        SettingContainerConfig moduleSettingConfig = new SettingContainerConfig(registeredModule.settings(), config);
        this.moduleSettings.put(name, moduleSettingConfig);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMinionModuleRegistration(MinionModuleRegistrationEvent event) {
        event.registerModule(DefaultMinionModules.APPEARANCE, AppearanceModule::new, AppearanceModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.POTION_EFFECT, PotionEffectModule::new, PotionEffectModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BEE_KEEPING, BeeKeepingModule::new, BeeKeepingModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BLOCK_FILTER, BlockFilterModule::new, BlockFilterModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BLOCK_BREAK, BlockBreakModule::new, BlockBreakModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.COMMUNICATION, CommunicationModule::new, CommunicationModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.EXPERIENCE_PICKUP, ExperiencePickupModule::new, ExperiencePickupModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.FARMING, FarmingModule::new, FarmingModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.ITEM_FILTER, ItemFilterModule::new, ItemFilterModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.FISHING, FishingModule::new, FishingModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.INVENTORY, InventoryModule::new, InventoryModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.ITEM_PICKUP, ItemPickupModule::new, ItemPickupModule.Settings.INSTANCE);
//        event.registerModule(DefaultMinionModules.MINER, MinerModule::new, MinerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BLOCK_PLACE, BlockPlaceModule::new, BlockPlaceModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.SHEARING, ShearingModule::new, ShearingModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.ATTACKING, AttackingModule::new, AttackingModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.UPGRADE, UpgradeModule::new, UpgradeModule.Settings.INSTANCE);
    }

    public record RegisteredMinionModule<T extends MinionModule>(String name,
                                                                 Function<Minion, T> factory,
                                                                 PDCSettingHolder settings) { }

}
