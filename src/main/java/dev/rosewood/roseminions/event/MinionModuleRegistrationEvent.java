package dev.rosewood.roseminions.event;

import dev.rosewood.roseminions.minion.module.MinionModule;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MinionModuleRegistrationEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Set<Class<? extends MinionModule>> registeredModules;

    public MinionModuleRegistrationEvent() {
        this.registeredModules = new HashSet<>();
    }

    /**
     * Adds a module to be registered
     *
     * @param moduleClass the class of the module to register
     * @return true if registering the module overwrote an existing module, false otherwise
     */
    public boolean registerModule(Class<? extends MinionModule> moduleClass) {
        return this.registeredModules.add(moduleClass);
    }

    /**
     * @return the set of registered modules
     */
    @NotNull
    public Set<Class<? extends MinionModule>> getRegisteredModules() {
        return this.registeredModules;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
