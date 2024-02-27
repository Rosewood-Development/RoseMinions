package dev.rosewood.roseminions.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import java.util.Collection;
import java.util.List;

public class MinionDataArgumentHandler extends RoseCommandArgumentHandler<MinionConfig> {

    public MinionDataArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, MinionConfig.class);
    }

    @Override
    protected MinionConfig handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.next();
        MinionConfig value = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(input);
        if (value == null)
            throw new HandledArgumentException("argument-handler-minion-data", StringPlaceholders.of("input", input));

        argumentParser.setContextValue(MinionConfig.class, value);
        return value;
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();

        Collection<String> minionTypes = this.rosePlugin.getManager(MinionTypeManager.class).getMinionTypes();
        if (minionTypes.isEmpty())
            return List.of("<no loaded minion types>");

        return minionTypes.stream()
                .map(x -> x.replace(' ', '_'))
                .toList();
    }

}
