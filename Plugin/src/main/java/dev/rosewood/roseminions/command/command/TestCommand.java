package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.roseminions.manager.MinionManager;
import java.util.ArrayList;

public class TestCommand extends RoseCommand {

    public TestCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);
        new ArrayList<>(minionManager.getLoadedMinions()).forEach(minionManager::destroyMinion);
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
