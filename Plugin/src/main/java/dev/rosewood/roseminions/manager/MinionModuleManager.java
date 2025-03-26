package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.MinionModuleRegistrationEvent;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.MinionItem;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.config.SettingsContainerConfig;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.module.BeaconModule;
import dev.rosewood.roseminions.minion.module.BeeKeeperModule;
import dev.rosewood.roseminions.minion.module.BreakerModule;
import dev.rosewood.roseminions.minion.module.CommunicatorModule;
import dev.rosewood.roseminions.minion.module.DefaultMinionModules;
import dev.rosewood.roseminions.minion.module.ExperienceModule;
import dev.rosewood.roseminions.minion.module.FarmerModule;
import dev.rosewood.roseminions.minion.module.FilterModule;
import dev.rosewood.roseminions.minion.module.FisherModule;
import dev.rosewood.roseminions.minion.module.InventoryModule;
import dev.rosewood.roseminions.minion.module.ItemPickupModule;
import dev.rosewood.roseminions.minion.module.MinerModule;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.module.PlacerModule;
import dev.rosewood.roseminions.minion.module.ShearerModule;
import dev.rosewood.roseminions.minion.module.SlayerModule;
import dev.rosewood.roseminions.minion.module.UpgradeModule;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
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

    public MinionModuleManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.registeredModules = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, this.rosePlugin);

        // Some classes that store settings other than modules need to be statically initialized
        try {
            Class.forName(MinionItem.class.getName());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void reload() {
        MinionModuleRegistrationEvent event = new MinionModuleRegistrationEvent();
        Bukkit.getPluginManager().callEvent(event);

        for (Map.Entry<String, RegisteredMinionModule<?>> entry : event.getRegisteredModules().entrySet()) {
            this.createAndLoadModuleFile(entry.getKey(), entry.getValue());
            this.registeredModules.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void disable() {
        this.registeredModules.clear();
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

    public boolean isValidModule(String name) {
        return this.registeredModules.containsKey(name.toLowerCase());
    }

    public SettingsContainerConfig getSectionSettings(String name, ConfigurationSection section) {
        RegisteredMinionModule<?> registeredModule = this.registeredModules.get(name);
        if (registeredModule != null)
            return new SettingsContainerConfig(registeredModule.settings(), section);
        return null;
    }

    private void createAndLoadModuleFile(String name, RegisteredMinionModule<?> registeredModule) {
        File directory = new File(this.rosePlugin.getDataFolder(), "modules");
        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, name + ".yml");
        boolean changed = !file.exists();
        CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);

        for (SettingAccessor<?> accessor : registeredModule.settings().get()) {
            if (!config.contains(accessor.getKey())) {
                accessor.write(config);
                changed = true;
            }
            accessor.readDefault(config);
        }

        if (changed)
            config.save(file);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMinionModuleRegistration(MinionModuleRegistrationEvent event) {
        event.registerModule(DefaultMinionModules.APPEARANCE, AppearanceModule::new, AppearanceModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BEACON, BeaconModule::new, BeaconModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BEE_KEEPER, BeeKeeperModule::new, BeeKeeperModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.BREAKER, BreakerModule::new, BreakerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.COMMUNICATOR, CommunicatorModule::new, CommunicatorModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.EXPERIENCE, ExperienceModule::new, ExperienceModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.FARMER, FarmerModule::new, FarmerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.FILTER, FilterModule::new, FilterModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.FISHER, FisherModule::new, FisherModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.INVENTORY, InventoryModule::new, InventoryModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.ITEM_PICKUP, ItemPickupModule::new, ItemPickupModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.MINER, MinerModule::new, MinerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.PLACER, PlacerModule::new, PlacerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.SHEARER, ShearerModule::new, ShearerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.SLAYER, SlayerModule::new, SlayerModule.Settings.INSTANCE);
        event.registerModule(DefaultMinionModules.UPGRADE, UpgradeModule::new, UpgradeModule.Settings.INSTANCE);
    }

    public record RegisteredMinionModule<T extends MinionModule>(String name,
                                                                 Function<Minion, T> factory,
                                                                 ModuleSettings settings) { }

}
