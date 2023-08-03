package dev.rosewood.roseminions.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.manager.LocaleManager;
import dev.rosewood.roseminions.minion.MinionData;
import dev.rosewood.roseminions.minion.MinionRank;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand extends RoseCommand {

    public GiveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, Player player, MinionData minionType, MinionRank rank, @Optional Integer amount) {
        LocaleManager locale = this.rosePlugin.getManager(LocaleManager.class);

        if (rank.getRank() < 0 || rank.getRank() > minionType.getMaxRank()) {
            if (minionType.getMaxRank() == 0) {
                locale.sendMessage(player, "command-give-no-ranks");
            } else {
                locale.sendMessage(player, "command-give-invalid-rank", StringPlaceholders.of("max", minionType.getMaxRank()));
            }

            return;
        }

        if (amount == null || amount < 1) {
            amount = 1;
        }

        ItemStack itemStack = minionType.getRank(rank.getRank()).getItemStack(true).clone();
        itemStack.setAmount(amount);

        player.getInventory().addItem(itemStack);
        locale.sendMessage(player, "command-give-success", StringPlaceholders.builder("minion", minionType.getId())
                .add("amount", amount)
                .add("rank", rank)
                .add("player", player.getName())
                .build()
        );
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
