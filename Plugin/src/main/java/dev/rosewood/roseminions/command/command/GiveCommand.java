package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.roseminions.minion.MinionData;
import org.bukkit.entity.Player;

public class GiveCommand extends RoseCommand {

    public GiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, Player player, MinionData minionType, @Optional Integer rank) {
        if (rank == null)
            rank = 0;

        if (rank < 0 || rank > minionType.getMaxRank()) {
            if (minionType.getMaxRank() == 0) {
                context.getSender().sendMessage("There are no ranks available for that minion type.");
            } else {
                context.getSender().sendMessage("Invalid rank! Must be between 0 and " + minionType.getMaxRank());
            }
            return;
        }

        player.getInventory().addItem(minionType.getRank(rank).getItemStack(true));
        player.sendMessage("You have been given a " + minionType.getId() + " minion.");
    }

    @Override
    protected String getDefaultName() {
        return "give";
    }

    @Override
    public String getDescriptionKey() {
        return "command-give-description";
    }

    @Override
    public String getRequiredPermission() {
        return "roseminions.give";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

}
