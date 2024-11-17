package dev.rosewood.roseminions.command.argument;

import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.minion.config.RankConfig;

public final class MinionArgumentHandlers {

    public static final ArgumentHandler<MinionConfig> MINION_CONFIG = new MinionConfigArgumentHandler(RoseMinions.getInstance());
    public static final ArgumentHandler<RankConfig> MINION_RANK = new MinionRankArgumentHandler();

}
