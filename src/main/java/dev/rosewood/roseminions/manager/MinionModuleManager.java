package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.MinionModuleRegistrationEvent;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.module.MinionModuleInfo;
import dev.rosewood.roseminions.minion.module.SlayerModule;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
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
                MinionModuleInfo moduleInfo = moduleClass.getAnnotation(MinionModuleInfo.class);
                if (moduleInfo == null)
                    throw new IllegalStateException("MinionModuleInfo annotation not found on " + moduleClass.getName());

                String name = moduleInfo.name();
                Constructor<? extends MinionModule> constructor = moduleClass.getConstructor(Minion.class);
                this.moduleConstructors.put(name, constructor);

                // Force class static initializer to run so the settings are registered
                // TODO: Use a required static method to initialize these instead so they can be wiped and re-registered
                Class.forName(moduleClass.getName());
                this.createModuleFile(name, moduleClass);
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

    private void createModuleFile(String name, Class<? extends MinionModule> moduleClass) {
        File directory = new File(this.rosePlugin.getDataFolder(), DIRECTORY);
        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, name + ".yml");
        boolean changed = !file.exists();
        CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);

        for (SettingsContainer.DefaultSettingItem<?> settingItem : SettingsContainer.REGISTERED_SETTINGS.get(moduleClass)) {
            if (!config.contains(settingItem.key())) {
                settingItem.write(config);
                changed = true;
            }
        }

        if (changed)
            config.save();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMinionModuleRegistration(MinionModuleRegistrationEvent event) {
        event.registerModule(SlayerModule.class);
    }

}
