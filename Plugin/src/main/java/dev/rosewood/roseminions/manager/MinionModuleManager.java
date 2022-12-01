package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.MinionModuleRegistrationEvent;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.module.FilterModule;
import dev.rosewood.roseminions.minion.module.FisherModule;
import dev.rosewood.roseminions.minion.module.InventoryModule;
import dev.rosewood.roseminions.minion.module.ItemPickupModule;
import dev.rosewood.roseminions.minion.module.MinerModule;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.module.MinionModuleInfo;
import dev.rosewood.roseminions.minion.module.SlayerModule;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MinionModuleManager extends Manager implements Listener {

    private static final String DIRECTORY = "modules";

    private final Map<String, Constructor<? extends MinionModule>> moduleConstructors;

    public MinionModuleManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.moduleConstructors = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, this.rosePlugin);
    }

    @Override
    public void reload() {
        MinionModuleRegistrationEvent event = new MinionModuleRegistrationEvent();
        Bukkit.getPluginManager().callEvent(event);

        for (Class<? extends MinionModule> moduleClass : event.getRegisteredModules()) {
            try {
                MinionModuleInfo moduleInfo = moduleClass.getDeclaredAnnotation(MinionModuleInfo.class);
                if (moduleInfo == null)
                    throw new IllegalStateException("MinionModuleInfo annotation not found on " + moduleClass.getName());

                String name = moduleInfo.name();
                Constructor<? extends MinionModule> constructor = moduleClass.getDeclaredConstructor(Minion.class);
                constructor.setAccessible(true);
                this.moduleConstructors.put(name, constructor);
                this.createAndLoadModuleFile(name, moduleClass);
            } catch (ReflectiveOperationException e) {
                this.rosePlugin.getLogger().warning("Failed to register module " + moduleClass.getName() + "!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disable() {
        this.moduleConstructors.clear();
    }

    public MinionModule createModule(String name, Minion minion) {
        Constructor<? extends MinionModule> constructor = this.moduleConstructors.get(name.toLowerCase());
        if (constructor == null)
            return null;

        try {
            return constructor.newInstance(minion);
        } catch (ReflectiveOperationException e) {
            this.rosePlugin.getLogger().warning("Failed to create module " + name.toLowerCase() + "!");
            e.printStackTrace();
        }

        return null;
    }

    public boolean isValidModule(String name) {
        return this.moduleConstructors.containsKey(name.toLowerCase());
    }

    public SettingsContainer getSectionSettings(String name, ConfigurationSection section) {
        Constructor<? extends MinionModule> constructor = this.moduleConstructors.get(name.toLowerCase());
        if (constructor == null)
            throw new IllegalArgumentException("Invalid module name: " + name);

        Class<? extends MinionModule> moduleClass = constructor.getDeclaringClass();
        SettingsContainer settingsContainer = new SettingsContainer(moduleClass);
        settingsContainer.loadDefaultsFromConfig(section);

        return settingsContainer;
    }

    private void createAndLoadModuleFile(String name, Class<? extends MinionModule> moduleClass) {
        File directory = new File(this.rosePlugin.getDataFolder(), DIRECTORY);
        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, name + ".yml");
        boolean changed = !file.exists();
        CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);

        for (SettingAccessor<?> accessor : SettingsContainer.REGISTERED_SETTINGS.get(moduleClass)) {
            if (!config.contains(accessor.getKey())) {
                accessor.write(config);
                changed = true;
            }
            accessor.readDefault(config);
        }

        if (changed)
            config.save();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMinionModuleRegistration(MinionModuleRegistrationEvent event) {
        event.registerModule(AppearanceModule.class);
        event.registerModule(SlayerModule.class);
        event.registerModule(MinerModule.class);
        event.registerModule(FisherModule.class);
        event.registerModule(ItemPickupModule.class);
        event.registerModule(InventoryModule.class);
        event.registerModule(FilterModule.class);
    }

}
