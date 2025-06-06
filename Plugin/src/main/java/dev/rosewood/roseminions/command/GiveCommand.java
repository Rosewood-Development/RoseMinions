package dev.rosewood.roseminions.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.command.argument.MinionArgumentHandlers;
import dev.rosewood.roseminions.manager.LocaleManager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.minion.config.RankConfig;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GiveCommand extends BaseRoseCommand {

    public GiveCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @RoseExecutable
    public void execute(CommandContext context, Player player, MinionConfig minionType, RankConfig rank, Integer amount) {
        LocaleManager locale = this.rosePlugin.getManager(LocaleManager.class);

        if (rank == null)
            rank = minionType.getDefaultRank();

        if (amount == null || amount < 1)
            amount = 1;

        ItemStack itemStack = minionType.getRank(rank.rank()).getDisplayItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
            pdc.set(MinionUtils.MINION_NEW_TYPE_KEY, PersistentDataType.STRING, minionType.getId());
            pdc.set(MinionUtils.MINION_NEW_RANK_KEY, PersistentDataType.STRING, rank.rank());
            itemStack.setItemMeta(itemMeta);
        }

        amount = Math.min(itemStack.getMaxStackSize(), amount);
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
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("give")
                .descriptionKey("command-give-description")
                .permission("roseminions.give")
                .arguments(ArgumentsDefinition.builder()
                        .required("player", ArgumentHandlers.PLAYER)
                        .required("minionType", MinionArgumentHandlers.MINION_CONFIG)
                        .required("rank", MinionArgumentHandlers.MINION_RANK)
                        .optional("amount", ArgumentHandlers.INTEGER)
                        .build())
                .build();
    }

}
