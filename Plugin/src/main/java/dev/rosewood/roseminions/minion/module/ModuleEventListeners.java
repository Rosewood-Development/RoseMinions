package dev.rosewood.roseminions.minion.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.event.Event;

public class ModuleEventListeners {

    private final Map<Class<? extends Event>, Consumer<? extends Event>> eventListeners;

    public ModuleEventListeners() {
        this.eventListeners = new HashMap<>();
    }

    public <T extends Event> void registerListener(Class<T> eventClass, Consumer<T> listener) {
        this.eventListeners.put(eventClass, listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void handleEvent(T event) {
        Class<? extends Event> eventClass = event.getClass();
        Consumer<? extends Event> consumer = this.eventListeners.get(eventClass);
        if (consumer != null)
            ((Consumer<T>) consumer).accept(event);
    }

}
