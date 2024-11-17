package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.roseminions.manager.MinionManager;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public class TestCommand extends BaseRoseCommand {

    public TestCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @RoseExecutable
    public void execute(CommandContext context, String player, boolean toggle) {
        context.getSender().sendMessage("Test: " + (player == null ? context.getSender().getName() : player) + " " + toggle);

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);

        // Remove active minions
        List.copyOf(minionManager.getLoadedMinions()).forEach(minionManager::destroyMinion);

        // Remove broken minions that are loaded
        Bukkit.getWorlds().stream()
                .flatMap(world -> world.getEntitiesByClass(ArmorStand.class).stream())
                .filter(minionManager::isMinion)
                .forEach(Entity::remove);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .permission("roseminions.test")
                .arguments(ArgumentsDefinition.builder()
                        .optional("player", ArgumentHandlers.forValues(String.class, "alice", "bob"), ArgumentCondition.hasPermission("roseminions.test.other"))
                        .required("toggle", ArgumentHandlers.BOOLEAN)
                        .build())
                .build();
    }

}
