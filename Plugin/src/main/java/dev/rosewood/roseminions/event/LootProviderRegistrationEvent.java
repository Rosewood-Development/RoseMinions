package dev.rosewood.roseminions.event;


import dev.rosewood.roseminions.hook.loot.LootProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class LootProviderRegistrationEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Map<String, LootProvider> registeredProviders;

    public LootProviderRegistrationEvent() {
        this.registeredProviders = new HashMap<>();
    }

    /**
     * Adds a module to be registered
     *
     * @param plugin The plugin that the provider is providing support for
     * @param provider The loot provider to register
     * @return true if registering the module overwrote an existing module, false otherwise
     */
    public boolean registerProvider(Plugin plugin, LootProvider provider) {
        return this.registeredProviders.put(plugin.getName(), provider) != null;
    }

    /**
     * @return the map of registered providers
     */
    @NotNull
    public Map<String, LootProvider> getRegisteredProviders() {
        return Collections.unmodifiableMap(this.registeredProviders);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
