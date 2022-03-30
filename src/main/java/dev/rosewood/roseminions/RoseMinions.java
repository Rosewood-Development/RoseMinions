package dev.rosewood.roseminions;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.manager.CommandManager;
import dev.rosewood.roseminions.manager.ConfigurationManager;
import dev.rosewood.roseminions.manager.LocaleManager;
import java.util.Collections;
import java.util.List;

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
        super(-1, 12626, ConfigurationManager.class, null, LocaleManager.class, CommandManager.class);

        instance = this;
    }

    @Override
    public void enable() {
        if (NMSUtil.getVersionNumber() < 16)
            this.getLogger().severe(this.getDescription().getName() + " best supports 1.16 servers and newer. If you try to use part of the plugin that is not available for your current server version, expect to see some errors.");
    }

    @Override
    public void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Collections.emptyList();
    }

}
