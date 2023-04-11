package dev.rosewood.roseminions.event;

import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.MinionModule;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MinionModuleRegistrationEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Map<String, MinionModuleManager.RegisteredMinionModule<?>> registeredModules;

    public MinionModuleRegistrationEvent() {
        this.registeredModules = new HashMap<>();
    }

    /**
     * Adds a module to be registered
     *
     * @param name The name of the module being registered
     * @return true if registering the module overwrote an existing module, false otherwise
     */
    public <T extends MinionModule> boolean registerModule(String name, Function<Minion, T> moduleFactory, Class<T> moduleClass) {
        return this.registeredModules.put(name, new MinionModuleManager.RegisteredMinionModule<>(name, moduleFactory, moduleClass)) != null;
    }

    /**
     * @return the map of registered modules
     */
    @NotNull
    public Map<String, MinionModuleManager.RegisteredMinionModule<?>> getRegisteredModules() {
        return Collections.unmodifiableMap(this.registeredModules);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
