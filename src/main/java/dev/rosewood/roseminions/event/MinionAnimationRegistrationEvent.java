package dev.rosewood.roseminions.event;

import dev.rosewood.roseminions.minion.animation.MinionAnimation;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MinionAnimationRegistrationEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Set<Class<? extends MinionAnimation>> registeredAnimations;

    public MinionAnimationRegistrationEvent() {
        this.registeredAnimations = new HashSet<>();
    }

    /**
     * Adds an animation to be registered
     *
     * @param animationClass the class of the animation to register
     * @return true if registering the animation overwrote an existing animation, false otherwise
     */
    public boolean registerAnimation(Class<? extends MinionAnimation> animationClass) {
        return this.registeredAnimations.add(animationClass);
    }

    /**
     * @return the set of registered animations
     */
    @NotNull
    public Set<Class<? extends MinionAnimation>> getRegisteredAnimations() {
        return this.registeredAnimations;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
