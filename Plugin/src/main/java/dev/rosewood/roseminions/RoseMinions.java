package dev.rosewood.roseminions;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.config.Settings;
import dev.rosewood.roseminions.listener.EntitiesLoadListener;
import dev.rosewood.roseminions.listener.MinionPickupListener;
import dev.rosewood.roseminions.listener.MinionPlaceListener;
import dev.rosewood.roseminions.listener.WorldListener;
import dev.rosewood.roseminions.manager.CommandManager;
import dev.rosewood.roseminions.manager.HookProviderManager;
import dev.rosewood.roseminions.manager.LocaleManager;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.nms.NMSAdapter;
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
        super(-1, 14807, null, LocaleManager.class, CommandManager.class);

        instance = this;
    }

    @Override
    public void enable() {
        if (!NMSAdapter.isValidVersion()) {
            this.getLogger().severe(this.getDescription().getName() + " only supports 1.19.4 servers and newer. Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
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
    protected SettingHolder getRoseConfigSettingHolder() {
        return Settings.INSTANCE;
    }

    @Override
    protected String[] getRoseConfigHeader() {
        return new String[]{
                "__________                       _____   __        __",
                "\\______   \\ ____  ______ ____   /     \\ |__| ____ |__| ____   ____   ______",
                " |       _//  _ \\/  ___// __ \\ /  \\ /  \\|  |/    \\|  |/  _ \\ /    \\ /  ___/",
                " |    |   (  <_> )___ \\\\  ___//    Y    \\  |   |  \\  (  <_> )   |  \\\\___ \\",
                " |____|_  /\\____/____  >\\___  >____|__  /__|___|  /__|\\____/|___|  /____  >",
                "        \\/           \\/     \\/        \\/        \\/               \\/     \\/"
        };
    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                MinionModuleManager.class,
                MinionTypeManager.class,
                MinionManager.class,
                HookProviderManager.class
        );
    }

}
