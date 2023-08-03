package dev.rosewood.roseminions.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.minion.MinionRank;

import java.util.ArrayList;
import java.util.List;

public class MinionRankArgumentHandler extends RoseCommandArgumentHandler<MinionRank> {

    public MinionRankArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, MinionRank.class);
    }

    @Override
    protected MinionRank handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();

        MinionData minionData = argumentParser.getContextValue(MinionData.class);
        if (minionData == null)
            throw new IllegalStateException("MinionRank argument handler requires a MinionData context value");

        int rankNumber;
        try {
            rankNumber = Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
            throw new HandledArgumentException("argument-handler-minion-rank", StringPlaceholders.of("input", input, "max", minionData.getMaxRank()));
        }

        MinionRank rank = minionData.getRank(rankNumber);
        if (rank == null)
            throw new HandledArgumentException("argument-handler-minion-rank", StringPlaceholders.of("input", input, "max", minionData.getMaxRank()));

        argumentParser.setContextValue(MinionRank.class, rank);
        return rank;
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        String input = argumentParser.previous();
        argumentParser.next();

        MinionData minionData = this.rosePlugin.getManager(MinionTypeManager.class).getMinionData(input);
        if (minionData == null)
            throw new IllegalStateException("MinionRank argument handler requires a MinionData context value");

        int max = minionData.getMaxRank();
        List<String> suggestions = new ArrayList<>();

        for (int i = 0; i < max + 1; i++) {
            suggestions.add(String.valueOf(i));
        }

        return suggestions;
    }

}
