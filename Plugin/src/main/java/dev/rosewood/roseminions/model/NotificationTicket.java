package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.module.MinionModule;
import java.util.function.Supplier;

public class NotificationTicket {

    private final MinionModule owningModule;
    private final String id;
    private final String message;
    private final long duration;
    private final Supplier<Boolean> visibilitySupplier;
    private final Supplier<StringPlaceholders> placeholderSupplier;

    public NotificationTicket(MinionModule owningModule, String id, String message, long duration, Supplier<Boolean> visibilitySupplier, Supplier<StringPlaceholders> placeholderSupplier) {
        this.owningModule = owningModule;
        this.id = id;
        this.message = message;
        this.duration = duration;
        this.visibilitySupplier = visibilitySupplier;
        this.placeholderSupplier = placeholderSupplier;
    }

    public boolean isFor(MinionModule module) {
        return this.owningModule == module;
    }

    public boolean isFor(MinionModule module, String id) {
        return this.owningModule == module && this.id.equals(id);
    }

    public long getDuration() {
        return this.duration;
    }

    public boolean isVisible() {
        return this.visibilitySupplier.get();
    }

    /**
     * Gets the message calls the placeholder supplier to apply them
     *
     * @return The message with placeholders applied
     */
    public String getMessage() {
        return this.placeholderSupplier.get().apply(this.message);
    }

}
