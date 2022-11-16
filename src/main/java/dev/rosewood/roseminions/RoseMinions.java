package dev.rosewood.roseminions;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.ClassUtils;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.listener.EntitiesLoadListener;
import dev.rosewood.roseminions.listener.MinionPickupListener;
import dev.rosewood.roseminions.listener.MinionPlaceListener;
import dev.rosewood.roseminions.listener.WorldListener;
import dev.rosewood.roseminions.manager.CommandManager;
import dev.rosewood.roseminions.manager.ConfigurationManager;
import dev.rosewood.roseminions.manager.DataManager;
import dev.rosewood.roseminions.manager.LocaleManager;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

/**
 * @author Esophose
 */
public class RoseMinions extends RosePlugin {

    /**
     * The running instance of RoseMinions on the server
     */
    private static RoseMinions instance;

    public static RoseMinions getInstance() {
        return instance;
    }

    public RoseMinions() {
        super(-1, 14807, ConfigurationManager.class, DataManager.class, LocaleManager.class, CommandManager.class);

        instance = this;

        // Force all RoseMinions classes in the jar to initialize
        ClassUtils.getClassesOf(this, "dev.rosewood.roseminions", Object.class);
    }

    @Override
    public void enable() {
        if (NMSUtil.getVersionNumber() < 16) {
            this.getLogger().severe(this.getDescription().getName() + " only supports 1.16.5 servers and newer. Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (NMSUtil.getVersionNumber() > 16) {
            pluginManager.registerEvents(new EntitiesLoadListener(this), this);
        } else {
            pluginManager.registerEvents(new WorldListener(this), this);
        }

        pluginManager.registerEvents(new MinionPlaceListener(this), this);
        pluginManager.registerEvents(new MinionPickupListener(this), this);
    }

    @Override
    public void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                MinionModuleManager.class,
                MinionTypeManager.class,
                MinionManager.class
        );
    }

}
