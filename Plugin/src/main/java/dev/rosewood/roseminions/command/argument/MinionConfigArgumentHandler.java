package dev.rosewood.roseminions.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import java.util.Collection;
import java.util.List;

public class MinionConfigArgumentHandler extends ArgumentHandler<MinionConfig> {

    private final RosePlugin rosePlugin;

    public MinionConfigArgumentHandler(RosePlugin rosePlugin) {
        super(MinionConfig.class);
        this.rosePlugin = rosePlugin;
    }

    @Override
    public MinionConfig handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        MinionConfig value = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(input);
        if (value == null)
            throw new HandledArgumentException("argument-handler-minion-config", StringPlaceholders.of("input", input));

        return value;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        Collection<String> minionTypes = this.rosePlugin.getManager(MinionTypeManager.class).getMinionTypes();
        if (minionTypes.isEmpty())
            return List.of("<no loaded minion types>");

        return minionTypes.stream()
                .map(x -> x.replace(' ', '_'))
                .toList();
    }

}
