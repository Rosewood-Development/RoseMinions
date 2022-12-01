package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.roseminions.manager.MinionManager;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public class TestCommand extends RoseCommand {

    public TestCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);

        // Remove active minions
        new ArrayList<>(minionManager.getLoadedMinions()).forEach(minionManager::destroyMinion);

        // Remove broken minions that are loaded
        Bukkit.getWorlds().stream()
                .flatMap(world -> world.getEntitiesByClass(ArmorStand.class).stream())
                .filter(minionManager::isMinion)
                .forEach(Entity::remove);
    }

    @Override
    protected String getDefaultName() {
        return "test";
    }

    @Override
    public String getDescriptionKey() {
        return "command-test-description";
    }

    @Override
    public String getRequiredPermission() {
        return null;
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

}
