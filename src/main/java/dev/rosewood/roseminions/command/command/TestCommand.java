package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.minion.Minion;
import org.bukkit.entity.Player;

public class TestCommand extends RoseCommand {

    public TestCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        Player player = (Player) context.getSender();
        HexUtils.sendMessage(player, "&aSpawning minion");
        Minion minion = new Minion(player.getUniqueId(), player.getLocation().getBlock().getLocation().add(0.5, 1.0, 0.5), false);
        this.rosePlugin.getManager(MinionManager.class).registerMinion(minion);
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
