package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.minion.Minion;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.bukkit.entity.ArmorStand;

public class MinionTypeManager extends Manager {

    private static final String DIRECTORY = "minions";

    private final Map<String, BiFunction<ArmorStand, byte[], Minion>> minionTypeConstructors;

    public MinionTypeManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.minionTypeConstructors = new HashMap<>();
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

}
