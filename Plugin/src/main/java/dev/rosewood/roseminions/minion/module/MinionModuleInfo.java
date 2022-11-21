package dev.rosewood.roseminions.minion.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a class as a minion module.
 * The class must have an empty public static method called init.
 * The class must also have a constructor that takes a Minion as its only parameter.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinionModuleInfo {

    String name();

}
