package dev.rosewood.roseminions.command.argument;

import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.minion.config.RankConfig;
import java.util.List;
import java.util.stream.Collectors;

public class MinionRankArgumentHandler extends ArgumentHandler<RankConfig> {

    public MinionRankArgumentHandler() {
        super(RankConfig.class);
    }

    @Override
    public RankConfig handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        MinionConfig minionConfig = context.get(MinionConfig.class);
        if (minionConfig == null)
            throw new IllegalStateException("MinionRank argument handler requires a MinionData context value");

        RankConfig rank = minionConfig.getRank(input);
        if (rank == null) {
            throw new HandledArgumentException("argument-handler-minion-rank", StringPlaceholders.of("input", input, "ranks", minionConfig.getRanks().stream()
                    .map(RankConfig::rank)
                    .collect(Collectors.joining(", "))));
        }

        return rank;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        MinionConfig minionConfig = context.get(MinionConfig.class);
        if (minionConfig == null)
            return List.of();

        return minionConfig.getRanks().stream()
                .map(RankConfig::rank)
                .toList();
    }

}
