package dev.rosewood.roseminions.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.minion.config.RankConfig;
import java.util.List;
import java.util.stream.Collectors;

public class MinionRankArgumentHandler extends RoseCommandArgumentHandler<RankConfig> {

    public MinionRankArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, RankConfig.class);
    }

    @Override
    protected RankConfig handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();

        MinionConfig minionConfig = argumentParser.getContextValue(MinionConfig.class);
        if (minionConfig == null)
            throw new IllegalStateException("MinionRank argument handler requires a MinionData context value");

        RankConfig rank = minionConfig.getRank(input);
        if (rank == null)
            throw new HandledArgumentException("argument-handler-minion-rank", StringPlaceholders.of("input", input, "ranks", minionConfig.getRanks().stream().map(RankConfig::rank).collect(Collectors.joining(", "))));

        argumentParser.setContextValue(RankConfig.class, rank);
        return rank;
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.previous();
        argumentParser.next();

        MinionConfig minionConfig = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(input);
        if (minionConfig == null)
            return List.of();

        return minionConfig.getRanks().stream()
                .map(RankConfig::rank)
                .toList();
    }

}
