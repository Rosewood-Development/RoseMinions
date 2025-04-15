package dev.rosewood.roseminions.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.HelpCommand;
import dev.rosewood.rosegarden.command.PrimaryCommand;
import dev.rosewood.rosegarden.command.ReloadCommand;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandInfo;

public class BaseCommand extends PrimaryCommand {

    public BaseCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("rm")
                .aliases("roseminions", "minions")
                .arguments(ArgumentsDefinition.builder()
                        .optionalSub("subcommand",
                                new HelpCommand(this.rosePlugin, this),
                                new ReloadCommand(this.rosePlugin),
                                new GiveCommand(this.rosePlugin)
                        ))
                .build();
    }

}
