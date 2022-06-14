package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.MinionModuleRegistrationEvent;
import dev.rosewood.roseminions.minion.module.SlayerModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class MinionAnimationManager extends Manager {

    public MinionAnimationManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMinionAnimationRegistration(MinionModuleRegistrationEvent event) {
        event.registerModule(SlayerModule.class);
    }

}
