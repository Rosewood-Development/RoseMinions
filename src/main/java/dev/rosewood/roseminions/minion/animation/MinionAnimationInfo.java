package dev.rosewood.roseminions.minion.animation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a class as a minion animation.
 * The class must have a static method called init that registers all the animation's settings.
 * The class must also have a constructor that takes a Minion as its only parameter.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinionAnimationInfo {

    String name();

}
