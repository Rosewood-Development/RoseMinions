package dev.rosewood.roseminions.object;

import dev.rosewood.roseminions.minion.module.MinionModule;
import java.util.Optional;

public interface Modular {

    /**
     * Gets the module of the given type.
     * Implementation should inherit from any parent Modular object.
     *
     * @param moduleClass the module class
     * @param <T> the module type
     * @return the module
     */
    <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass);

}
